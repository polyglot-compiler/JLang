package polyllvm.extension;

import polyglot.ast.Catch;
import polyglot.ast.Node;
import polyglot.ast.Try;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMTryExt extends PolyLLVMExt {

    /**
     * An exception frame holds information relevant to a try-catch-finally block,
     * such as the catch landing pad. Translations use it to know where to send
     * exceptions and when to execute finally blocks.
     */
    public static class ExceptionFrame {
        private LLVMTranslator v;

        /**
         * Landing pad that jumps to a matching catch clause.
         * Null if no catch blocks, or if we have already passed through the try block.
         */
        private LLVMBasicBlockRef lpadCatch;

        /** Landing pad that jumps to the finally block. Null if no finally block. */
        private LLVMBasicBlockRef lpadFinally;

        /**
         * Maps finally-block destinations to finally-block copies.
         * Becomes the identity map if there is no finally block (to prevent redundant jumps).
         */
        // There can be multiple branch destinations after a finally block because a finally
        // block can be entered after a break, continue, return, exception, or normal execution.
        // Making a full copy of the finally block for each destination trades some code bloat
        // for the sake of simplicity.
        private Map<LLVMBasicBlockRef, LLVMBasicBlockRef> finallyBlocks = new HashMap<>();

        private ExceptionFrame(
                LLVMTranslator v,
                LLVMBasicBlockRef lpadCatch,
                LLVMBasicBlockRef lpadFinally) {
            this.v = v;
            this.lpadCatch = lpadCatch;
            this.lpadFinally = lpadFinally;
        }

        public LLVMBasicBlockRef getLpadCatch() {
            return lpadCatch;
        }

        public LLVMBasicBlockRef getLpadFinally() {
            return lpadFinally;
        }

        /**
         * Returns the block ref to the finally-block copy which branches to [dest],
         * creating one if necessary. Returns [dest] if there is no finally block.
         */
        public LLVMBasicBlockRef getFinallyBlockBranchingTo(LLVMBasicBlockRef dest) {
            return lpadFinally == null
                    ? dest // If no finally block, just return [dest] directly.
                    : finallyBlocks.computeIfAbsent(dest, (key) -> {
                String destName = LLVMGetBasicBlockName(key).getString();
                return v.utils.buildBlock("finally.then." + destName);
            });
        }
    }

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Try n = (Try) node();

        // Useful functions, types, and constants.
        LLVMValueRef nullBytePtr = LLVMConstPointerNull(v.utils.llvmBytePtr());
        LLVMValueRef personalityFunc = v.utils.getFunction(
                v.mod, Constants.PERSONALITY_FUNC,
                v.utils.functionType(LLVMInt32TypeInContext(v.context)));
        LLVMValueRef extractJavaExnFunc = v.utils.getFunction(
                v.mod, Constants.EXTRACT_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMTypeRef bytePtr = v.utils.structType(
                v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)),
                LLVMInt32TypeInContext(v.context));
        LLVMValueRef throwExnFunc = v.utils.getFunction(v.mod, Constants.THROW_EXCEPTION,
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
        v.debugInfo.emitLocation(n.tryBlock());
        n.visitChild(n.tryBlock(), v);
        v.utils.branchUnlessTerminated(frame.getFinallyBlockBranchingTo(end));
        frame.lpadCatch = null; // We don't want future translations landing at this catch.

        if (!n.catchBlocks().isEmpty()) {

            // Even if no catch claus matches an in-flight exception, we must still stop unwinding
            // if (1) there is a finally block, or (2) there is an enclosing landing pad in this
            // same function. (1) is true because finally blocks might raise a new exception or
            // cancel the existing one (through a control transfer), and the unwinder disallows
            // both while unwinding. (2) is true for convenience only; in principle we could
            // include the outer catch clauses in the inner landing pad instruction, and jump
            // to the outer catch dispatch code if none of the inner catch clauses match.
            boolean mustStopUnwinding = n.finallyBlock() != null || lpadOuter != null;

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

            LLVMValueRef catchExn = LLVMBuildExtractValue(v.builder, lpadCatchRes, 0, "exn");
            LLVMValueRef catchSel = LLVMBuildExtractValue(v.builder, lpadCatchRes, 1, "sel");

            // Translate catch blocks.
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

                // Build catch block.
                LLVMPositionBuilderAtEnd(v.builder, catchBlock);
                LLVMTypeRef exnType = v.utils.toLL(cb.catchType().toReference());
                LLVMValueRef exnVar = PolyLLVMLocalDeclExt.createLocal(
                        v, cb.formal().name(), exnType);
                v.addAllocation(cb.formal().name(), exnVar);
                LLVMValueRef jexn = v.utils.buildFunCall(extractJavaExnFunc, catchExn);
                LLVMValueRef castJExn = LLVMBuildBitCast(v.builder, jexn, exnType, "cast");
                LLVMBuildStore(v.builder, castJExn, exnVar);
                v.debugInfo.emitLocation(cb);
                n.visitChild(cb, v);
                v.utils.branchUnlessTerminated(frame.getFinallyBlockBranchingTo(end));

                LLVMPositionBuilderAtEnd(v.builder, catchNext);
            }

            if (mustStopUnwinding) {
                // We temporarily caught the exception using a catch-all clause.
                // Rethrow the exception after running the finally block (if any).
                LLVMBasicBlockRef catchRethrow = v.utils.buildBlock("rethrow");
                LLVMBuildBr(v.builder, frame.getFinallyBlockBranchingTo(catchRethrow));
                LLVMPositionBuilderAtEnd(v.builder, catchRethrow);
                v.utils.buildProcCall(lpadOuter, throwExnFunc, catchExn);
                LLVMBuildUnreachable(v.builder);
            } else {
                // We did not catch the exception. Resume unwinding.
                LLVMBuildResume(v.builder, lpadCatchRes);
            }

        }

        if (n.finallyBlock() != null) {

            // Build finally landing pad. This handles exceptions thrown from within a catch block.
            LLVMPositionBuilderAtEnd(v.builder, lpadFinally);
            LLVMValueRef lpadFinallyRes = LLVMBuildLandingPad(
                    v.builder, bytePtr, personalityFunc, /*numClauses*/ 1, "lpad.finally.res");
            LLVMAddClause(lpadFinallyRes, nullBytePtr); // Catch-all clause.
            LLVMValueRef finallyExn = LLVMBuildExtractValue(v.builder, lpadFinallyRes, 0, "exn");

            // Build block to rethrow the exception once the finally block finishes.
            LLVMBasicBlockRef finallyRethrow = v.utils.buildBlock("rethrow");
            LLVMBuildBr(v.builder, frame.getFinallyBlockBranchingTo(finallyRethrow));
            LLVMPositionBuilderAtEnd(v.builder, finallyRethrow);
            v.utils.buildProcCall(lpadOuter, throwExnFunc, finallyExn);
            LLVMBuildUnreachable(v.builder);

            // Pop the exception frame before translating the finally blocks
            // since we want the translations to use the outer landing pad for any
            // exceptions thrown within the finally block.
            v.popExceptionFrame();

            // Build finally blocks (one copy for each possible control flow destination).
            for (Entry<LLVMBasicBlockRef, LLVMBasicBlockRef> entry
                    : frame.finallyBlocks.entrySet()) {
                LLVMBasicBlockRef dest = entry.getKey();
                LLVMBasicBlockRef head = entry.getValue();
                LLVMPositionBuilderAtEnd(v.builder, head);
                v.debugInfo.emitLocation(n.finallyBlock());
                n.visitChild(n.finallyBlock(), v);
                v.utils.branchUnlessTerminated(dest);
            }
        } else {
            v.popExceptionFrame();
        }

        LLVMPositionBuilderAtEnd(v.builder, end);
        return n;
    }
}
