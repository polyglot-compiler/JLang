package polyllvm.extension;

import polyglot.ast.Catch;
import polyglot.ast.Node;
import polyglot.ast.Try;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Translates try-catch blocks.
 *
 * Exception handling is one of the harder aspects of LLVM, so read the LLVM documentation on
 * exception handling very carefully. The basic idea is this: within a try block, all
 * function calls must specify a "landing pad," which is just a basic block that processes
 * a thrown exception. The landing pad must examine the exception to see if it matches any of the
 * catch clauses. If no clauses are matched, we run the finally block and rethrow the exception.
 *
 * The "unwinder" is the library code that actually walks up the stack and jumps to landing pads
 * when necessary. The unwinder calls into a language-defined "personality" function when
 * deciding whether to stop at a landing pad, which for us exists in jni/exceptions.cpp.
 * The personality function contains the call to instanceof (for example) to determine whether the
 * exception thrown matches one of the catch clauses.
 *
 * Java exception handling can get tricky. E.g., finally blocks can throw a new exception, thereby
 * cancelling the previous one; catch blocks can contain a break statement which jumps to a
 * label several try-catch-finally nesting levels away; etc. To combat this complexity, we maintain
 * that v.currLandingPad() always points to the landing pad of the try-catch block which should
 * gain control if an exception is thrown at the current position of the translator within the AST.
 * This might be a landing pad which dispatches to a catch block, or one which merely runs
 * the finally block and rethrows. To ensure that finally blocks will be run despite early returns
 * from a function (for example), we also maintain a stack of exception frames so that return
 * statements can run requisite finally blocks before emitting the ret instruction. This state is
 * held in the translator, but updated by this class during translation.
 */
public class PolyLLVMTryExt extends PolyLLVMExt {

    /**
     * An exception frame holds information relevant to a try-catch-finally block,
     * such as the catch landing pad. Translations use it to know where to send
     * exceptions and when to execute finally blocks.
     */
    public static class ExceptionFrame {
        private final LLVMTranslator v;

        /**
         * Landing pad that jumps to a matching catch clause.
         * Null if no catch blocks, or if we have already passed through the try block.
         */
        private LLVMBasicBlockRef lpadCatch;

        /**
         * Landing pad that jumps to the finally block. Null if no finally block,
         * or if we have already passed through all catch blocks.
         */
        private LLVMBasicBlockRef lpadFinally;

        /**
         * A stack-allocated integer that tracks where to go after the finally block finishes.
         * Ranges from 0 to one less than the number of finally block destinations.
         * At the end of a finally block, we switch on the value stored in this variable.
         * May be null if there is no finally block.
         */
        private LLVMValueRef destIdVar;

        /**
         * The finally block for this exception frame.
         * May be null if there is no finally block.
         */
        private LLVMBasicBlockRef finallyBlock;

        /**
         * Maps finally-block destinations to blocks which jump to the finally block
         * after setting the finally destination ID appropriately.
         * The ith destination block in this map has destination ID i.
         * May be null if there is no finally block.
         */
        private Map<LLVMBasicBlockRef, LLVMBasicBlockRef> destBlocks;

        private ExceptionFrame(
                LLVMTranslator v,
                LLVMBasicBlockRef lpadCatch,
                LLVMBasicBlockRef lpadFinally) {
            this.v = v;
            this.lpadCatch = lpadCatch;
            this.lpadFinally = lpadFinally;
            if (lpadFinally != null) {
                finallyBlock = v.utils.buildBlock("finally");
                destIdVar = v.utils.buildAlloca("finally.dest", LLVMInt32TypeInContext(v.context));
                destBlocks = new LinkedHashMap<>();
            }
        }

        public LLVMBasicBlockRef getLpadCatch() {
            return lpadCatch;
        }

        public LLVMBasicBlockRef getLpadFinally() {
            return lpadFinally;
        }

        /**
         * Returns a block which will run the finally block and then jump to [dest].
         * Note: returns [dest] if there is no finally block.
         */
        public LLVMBasicBlockRef getFinallyBlockBranchingTo(LLVMBasicBlockRef dest) {
            // If no finally block, jump directly to the destination.
            if (lpadFinally == null)
                return dest;

            // Otherwise, build an intermediate block which sets the finally
            // destination ID, then branches to the finally block.
            return destBlocks.computeIfAbsent(dest, (key) -> {
                LLVMBasicBlockRef prevBlock = LLVMGetInsertBlock(v.builder);

                String destName = LLVMGetBasicBlockName(key).getString();
                LLVMBasicBlockRef block = v.utils.buildBlock("finally.then." + destName);
                LLVMPositionBuilderAtEnd(v.builder, block);

                LLVMTypeRef intTy = LLVMInt32TypeInContext(v.context);
                LLVMValueRef destId = LLVMConstInt(intTy, destBlocks.size(), /*sign-extend*/ 0);
                LLVMBuildStore(v.builder, destId, destIdVar);
                LLVMBuildBr(v.builder, finallyBlock);

                LLVMPositionBuilderAtEnd(v.builder, prevBlock);
                return block;
            });
        }
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Try n = (Try) node();

        // Useful functions, types, and constants.
        LLVMValueRef nullBytePtr = LLVMConstPointerNull(v.utils.llvmBytePtr());
        LLVMValueRef personalityFunc = v.utils.getFunction(
                Constants.PERSONALITY_FUNC,
                v.utils.functionType(LLVMInt32TypeInContext(v.context)));
        LLVMValueRef extractJavaExnFunc = v.utils.getFunction(
                Constants.EXTRACT_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMTypeRef bytePtr = v.utils.structType(
                v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)),
                LLVMInt32TypeInContext(v.context));
        LLVMValueRef throwExnFunc = v.utils.getFunction(Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidTypeInContext(v.context), v.utils.llvmBytePtr()));

        // Useful blocks, null if not needed.
        LLVMBasicBlockRef lpadCatch = !n.catchBlocks().isEmpty()
                ? v.utils.buildBlock("lpad.catch")
                : null;
        LLVMBasicBlockRef lpadFinally = n.finallyBlock() != null
                ? v.utils.buildBlock("lpad.finally")
                : null;
        LLVMBasicBlockRef end = v.utils.buildBlock("try.end");

        // Push exception frame, which holds blocks useful for child translations.
        LLVMBasicBlockRef lpadOuter = v.currLandingPad();
        ExceptionFrame frame = new ExceptionFrame(v, lpadCatch, lpadFinally);
        v.pushExceptionFrame(frame);

        // Build try block.
        n.visitChild(n.tryBlock(), v);
        v.utils.branchUnlessTerminated(frame.getFinallyBlockBranchingTo(end));

        // Prevent future translations landing at this catch.
        // For example, exceptions thrown within the catch blocks should either land at the
        // finally block landing pad, or otherwise at an enclosing try-catch landing pad.
        frame.lpadCatch = null;

        // Even if no catch claus matches an in-flight exception, we must still stop unwinding
        // if (1) there is a finally block, or (2) there is an enclosing landing pad in this
        // same function. (1) is true because finally blocks might raise a new exception or
        // cancel the existing one (through a control transfer), and the unwinder disallows
        // both while unwinding. (2) is true for convenience only; in principle we could
        // include the outer catch clauses in the inner landing pad instruction, and jump
        // to the outer catch dispatch code if none of the inner catch clauses match.
        boolean mustStopUnwinding = n.finallyBlock() != null || lpadOuter != null;

        // If we must stop unwinding, then we may need to rethrow the in-flight exception.
        // Hold that exception on the stack.
        LLVMValueRef rethrowExnVar = mustStopUnwinding
                ? v.utils.buildAlloca("rethrow.exn", v.utils.llvmBytePtr())
                : null;

        if (!n.catchBlocks().isEmpty()) {

            // Build catch landing pad.
            LLVMPositionBuilderAtEnd(v.builder, lpadCatch);
            int numClauses = n.catchBlocks().size() + (mustStopUnwinding ? 1 : 0);
            LLVMValueRef lpadCatchRes = LLVMBuildLandingPad(
                    v.builder, bytePtr, personalityFunc, numClauses, "lpad.catch.res");
            n.catchBlocks().stream()
                    .map((cb) -> v.classObjs.toTypeIdentity(cb.catchType().toReference()))
                    .forEachOrdered((typeId) -> LLVMAddClause(lpadCatchRes, typeId));
            if (mustStopUnwinding)
                LLVMAddClause(lpadCatchRes, nullBytePtr); // Catch-all clause.

            // The exception value is an unwinder data structure that contains a reference
            // to the actual Java exception object.
            LLVMValueRef catchExn = LLVMBuildExtractValue(v.builder, lpadCatchRes, 0, "exn");

            // The selector value is the catch clause index that the unwinder claims is matched.
            LLVMValueRef catchSel = LLVMBuildExtractValue(v.builder, lpadCatchRes, 1, "sel");

            // Translate catch blocks.
            // Note that MultiCatch nodes are handled automatically because the type system
            // sets the catch type to the lowest upper bound.
            int catchIdx = 1;
            for (Catch cb : n.catchBlocks()) {

                // Extend dispatch chain.
                LLVMBasicBlockRef catchBlock = v.utils.buildBlock("catch." + cb.catchType());
                LLVMBasicBlockRef catchNext = v.utils.buildBlock("catch.next");
                LLVMValueRef typeId = LLVMConstInt(
                        LLVMInt32TypeInContext(v.context), catchIdx++, /*signExtend*/ 0);
                LLVMValueRef matches = LLVMBuildICmp(
                        v.builder, LLVMIntEQ, catchSel, typeId, "catch.matches");
                LLVMBuildCondBr(v.builder, matches, catchBlock, catchNext);

                // Declare catch block formal.
                LLVMPositionBuilderAtEnd(v.builder, catchBlock);
                LLVMTypeRef exnType = v.utils.toLL(cb.catchType().toReference());
                LLVMValueRef exnVar = v.utils.buildAlloca(cb.formal().name(), exnType);
                v.addTranslation(cb.formal().localInstance().orig(), exnVar);
                v.debugInfo.createLocalVariable(v, cb.formal(), exnVar);

                // Initialize catch block formal.
                LLVMValueRef jexn = v.utils.buildFunCall(extractJavaExnFunc, catchExn);
                LLVMValueRef castJExn = LLVMBuildBitCast(v.builder, jexn, exnType, "cast");
                LLVMBuildStore(v.builder, castJExn, exnVar);

                // Build catch block.
                n.visitChild(cb, v);
                v.utils.branchUnlessTerminated(frame.getFinallyBlockBranchingTo(end));

                LLVMPositionBuilderAtEnd(v.builder, catchNext);
            }

            if (mustStopUnwinding) {
                // We temporarily caught the exception using a catch-all clause.
                // Rethrow the exception after running the finally block (if any).
                LLVMBuildStore(v.builder, catchExn, rethrowExnVar);
                LLVMBasicBlockRef catchRethrow = v.utils.buildBlock("rethrow");
                LLVMBuildBr(v.builder, frame.getFinallyBlockBranchingTo(catchRethrow));
                LLVMPositionBuilderAtEnd(v.builder, catchRethrow);
                LLVMValueRef loadExn = LLVMBuildLoad(v.builder, rethrowExnVar, "load.rethrow.exn");
                v.utils.buildProcCall(lpadOuter, throwExnFunc, loadExn);
                LLVMBuildUnreachable(v.builder);
            } else {
                // We did not catch the exception. Resume unwinding.
                LLVMBuildResume(v.builder, lpadCatchRes);
            }

        }

        if (n.finallyBlock() != null) {

            assert frame.finallyBlock != null
                    && frame.destIdVar != null
                    && frame.destBlocks != null;

            // Build finally landing pad. This handles exceptions thrown within a catch block,
            // or within a try block when there are no catch clauses.
            LLVMPositionBuilderAtEnd(v.builder, lpadFinally);
            LLVMValueRef lpadFinallyRes = LLVMBuildLandingPad(
                    v.builder, bytePtr, personalityFunc, /*numClauses*/ 1, "lpad.finally.res");
            LLVMAddClause(lpadFinallyRes, nullBytePtr); // Catch-all clause.
            LLVMValueRef finallyExn = LLVMBuildExtractValue(v.builder, lpadFinallyRes, 0, "exn");

            // Build block to rethrow the exception once the finally block finishes.
            LLVMBuildStore(v.builder, finallyExn, rethrowExnVar);
            LLVMBasicBlockRef finallyRethrow = v.utils.buildBlock("rethrow");
            LLVMBuildBr(v.builder, frame.getFinallyBlockBranchingTo(finallyRethrow));
            LLVMPositionBuilderAtEnd(v.builder, finallyRethrow);
            LLVMValueRef loadExn = LLVMBuildLoad(v.builder, rethrowExnVar, "load.rethrow.exn");
            v.utils.buildProcCall(lpadOuter, throwExnFunc, loadExn);
            LLVMBuildUnreachable(v.builder);

            // We want the finally block translations to use the outer landing pad for any
            // exceptions thrown within the finally block.
            frame.lpadFinally = null;

            // Build finally block.
            LLVMPositionBuilderAtEnd(v.builder, frame.finallyBlock);
            n.visitChild(n.finallyBlock(), v);

            // Switch on the finally destination ID.
            LLVMBasicBlockRef switchBlock = v.utils.buildBlock("finally.dest.switch");
            v.utils.branchUnlessTerminated(switchBlock);

            // The default case of the destination switch is unreachable.
            LLVMBasicBlockRef unreachableBlock = v.utils.buildBlock("unreachable");
            LLVMPositionBuilderAtEnd(v.builder, unreachableBlock);
            LLVMBuildUnreachable(v.builder);

            LLVMPositionBuilderAtEnd(v.builder, switchBlock);
            LLVMValueRef destId = LLVMBuildLoad(v.builder, frame.destIdVar, "load.finally.dest");
            LLVMValueRef switchDest = LLVMBuildSwitch(
                    v.builder, destId, unreachableBlock, frame.destBlocks.size());
            int counter = 0;
            for (LLVMBasicBlockRef dest : frame.destBlocks.keySet()) {
                LLVMTypeRef intTy = LLVMInt32TypeInContext(v.context);
                LLVMValueRef catchDestId = LLVMConstInt(intTy, counter++, /*sign-extend*/ 0);
                LLVMAddCase(switchDest, catchDestId, dest);
            }
        }

        v.popExceptionFrame();
        LLVMPositionBuilderAtEnd(v.builder, end);
        return n;
    }
}
