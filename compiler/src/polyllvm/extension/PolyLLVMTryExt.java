package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.Catch;
import polyglot.ast.Node;
import polyglot.ast.Try;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.io.Serializable;

public class PolyLLVMTryExt extends PolyLLVMExt {

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        Try n = (Try) node();
        v.enterTry();

        Serializable s;

        LLVMTypeRef exnType = v.utils.structType(v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)), LLVMInt32TypeInContext(v.context));

        LLVMBasicBlockRef tryBlock = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "try_block");
        LLVMBasicBlockRef tryEnd = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "try_end");
        LLVMBasicBlockRef tryFinally = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "try_finally");

        LLVMValueRef finally_flag = PolyLLVMLocalDeclExt.createLocal(v, "finally_flag", LLVMInt1TypeInContext(v.context));

        v.debugInfo.emitLocation(n);

        LLVMBuildBr(v.builder, tryBlock);

        LLVMPositionBuilderAtEnd(v.builder, tryBlock);
        LLVMBuildStore(v.builder, LLVMConstInt(LLVMInt1TypeInContext(v.context), 0, /*sign-extend*/ 0), finally_flag);
        v.visitEdge(n, n.tryBlock());
        if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(v.builder)) == null) {
            LLVMBuildBr(v.builder, tryFinally);
        }

        LLVMPositionBuilderAtEnd(v.builder, v.currLpad());
        LLVMValueRef personalityFunc = v.utils.getFunction(v.mod, Constants.PERSONALITY_FUNC,
                v.utils.functionType(LLVMInt32TypeInContext(v.context)));
        LLVMValueRef lpad = LLVMBuildLandingPad(v.builder,
                exnType, LLVMConstBitCast(personalityFunc, v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context))),
                n.catchBlocks().size(), "lpad");
        n.catchBlocks().stream().forEach(cb ->
                LLVMAddClause(lpad, v.classObjs.classIdVarRef(v.mod, cb.catchType().toReference())));

        v.debugInfo.emitLocation();
        LLVMValueRef exn_slot = PolyLLVMLocalDeclExt.createLocal(v, "exn_slot", v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)));
        v.debugInfo.emitLocation(n);
        LLVMValueRef exn = LLVMBuildExtractValue(v.builder, lpad, 0, "exn");
        LLVMBuildStore(v.builder, exn, exn_slot);

        v.debugInfo.emitLocation();
        LLVMValueRef ehselector_slot = PolyLLVMLocalDeclExt.createLocal(v, "ehselector_slot", LLVMInt32TypeInContext(v.context));
        v.debugInfo.emitLocation(n);
        LLVMValueRef ehselector = LLVMBuildExtractValue(v.builder, lpad, 1, "ehselector");
        LLVMBuildStore(v.builder, ehselector, ehselector_slot);

        LLVMValueRef typeidFunc = v.utils.getFunction(v.mod, Constants.TYPEID_INTRINSIC,
                v.utils.functionType(LLVMInt32TypeInContext(v.context), v.utils.llvmBytePtr()));

        LLVMBasicBlockRef ehResume = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "eh_resume");
        LLVMPositionBuilderAtEnd(v.builder, ehResume);
        LLVMValueRef reumeExn = LLVMBuildLoad(v.builder, exn_slot, "exn");
        LLVMValueRef resumeSel = LLVMBuildLoad(v.builder, ehselector_slot, "sel");
        LLVMValueRef lpadVal = LLVMBuildInsertValue(v.builder, LLVMGetUndef(exnType), reumeExn, 0, "lpad.val");
        lpadVal = LLVMBuildInsertValue(v.builder, lpadVal, resumeSel, 1, "lpad.val");
        LLVMBuildResume(v.builder, lpadVal);


        LLVMPositionBuilderAtEnd(v.builder, v.currLpad());
        LLVMBasicBlockRef dispatch = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "catch_dispatch");
        LLVMBuildBr(v.builder, dispatch);

        //TODO: need a cleanup lpad for catch blocks
        v.setLpad(LLVMAppendBasicBlockInContext(v.context, v.currFn(), "cleanup_lpad"));
        LLVMPositionBuilderAtEnd(v.builder, v.currLpad());
        LLVMValueRef cleanup_lpad = LLVMBuildLandingPad(v.builder,
                exnType, LLVMConstBitCast(personalityFunc, v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context))),
                0, "cleanup_lpad");
        LLVMSetCleanup(cleanup_lpad,/*true*/1);
        LLVMValueRef exn_clean = LLVMBuildExtractValue(v.builder, cleanup_lpad, 0, "exn");
        LLVMBuildStore(v.builder, exn_clean, exn_slot);
        LLVMValueRef ehselector_clean = LLVMBuildExtractValue(v.builder, cleanup_lpad, 1, "ehselector");
        LLVMBuildStore(v.builder, ehselector_clean, ehselector_slot);
        LLVMBuildStore(v.builder, LLVMConstInt(LLVMInt1TypeInContext(v.context), 1, /*sign-extend*/ 0), finally_flag);
        LLVMBuildBr(v.builder, tryFinally);

        //Block to set finally flag to resume exception propogation
        LLVMBasicBlockRef setFinallyFlag = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "set_finally_flag");
        LLVMPositionBuilderAtEnd(v.builder, setFinallyFlag);
        LLVMBuildStore(v.builder, LLVMConstInt(LLVMInt1TypeInContext(v.context), 1, /*sign-extend*/ 0), finally_flag);
        LLVMBuildBr(v.builder, tryFinally);


        LLVMPositionBuilderAtEnd(v.builder, dispatch);
        for (int i = 0; i<n.catchBlocks().size(); i++) {
            Catch cb = n.catchBlocks().get(i);
            v.debugInfo.emitLocation(cb);
            LLVMBasicBlockRef catchBlock = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "catch_" + cb.catchType());
            LLVMPositionBuilderAtEnd(v.builder, catchBlock);
            v.visitEdge(n, cb);
            if (LLVMGetBasicBlockTerminator(LLVMGetInsertBlock(v.builder)) == null) {
                LLVMBuildBr(v.builder, tryFinally);
            }

            LLVMPositionBuilderAtEnd(v.builder, dispatch);
            LLVMValueRef sel = LLVMBuildLoad(v.builder, ehselector_slot, "sel");
            LLVMValueRef typeid = v.utils.buildMethodCall(typeidFunc,
                    v.classObjs.classIdVarRef(v.mod, cb.catchType().toReference()));
            LLVMValueRef matches = LLVMBuildICmp(v.builder, LLVMIntEQ, sel, typeid, "matches");
            if(i==n.catchBlocks().size() - 1){
                //Need to resume Exception handling if last catch does not match exception type
                LLVMBuildCondBr(v.builder, matches, catchBlock, setFinallyFlag);
            } else {
                dispatch = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "catch_dispatch");
                LLVMBuildCondBr(v.builder, matches, catchBlock, dispatch);
            }
        }

        v.exitTry();

        LLVMPositionBuilderAtEnd(v.builder, tryFinally);
        if(n.finallyBlock() != null){
            v.debugInfo.emitLocation(n.finallyBlock());
            v.visitEdge(n, n.finallyBlock());
        }
        LLVMBuildCondBr(v.builder, LLVMBuildLoad(v.builder, finally_flag, "flag"), ehResume, tryEnd);

        LLVMPositionBuilderAtEnd(v.builder, tryEnd);

        return n;
    }
}
