package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.ESeq;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMEseqExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ESeq n = (ESeq) node();
        v.addTranslation(n, v.getTranslation(n.expr()));
        return super.leaveTranslateLLVM(v);
    }
}
