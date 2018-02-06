package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Return n = (Return) node();
        Expr e = n.expr();

        // If we are within an exception frame, we may need to detour through finally blocks first.
        if (v.needsFinallyBlockChain()) {
            LLVMBasicBlockRef retBlock = v.utils.buildBlock("ret");
            LLVMBasicBlockRef firstFinallyBlock = v.buildFinallyBlockChain(retBlock, 0);
            LLVMBuildBr(v.builder, firstFinallyBlock);
            LLVMPositionBuilderAtEnd(v.builder, retBlock);
        }

        // Build the actual return.
        LLVMValueRef res = e == null
                ? LLVMBuildRetVoid(v.builder)
                : LLVMBuildRet(v.builder, v.getTranslation(e));
        v.addTranslation(n, res);

        return super.leaveTranslateLLVM(v);
    }
}
