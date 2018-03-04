package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.ESeq;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMEseqExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ESeq n = (ESeq) node();
        v.addTranslation(n, v.getTranslation(n.expr()));
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public void translateLLVMConditional(
            LLVMTranslator v, LLVMBasicBlockRef trueBlock, LLVMBasicBlockRef falseBlock) {
        ESeq n = (ESeq) node();
        n.visitList(n.statements(), v);
        lang().translateLLVMConditional(n.expr(), v, trueBlock, falseBlock);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        ESeq n = (ESeq) node();
        n.visitList(n.statements(), v);
        return lang().translateAsLValue(n.expr(), v);
    }
}
