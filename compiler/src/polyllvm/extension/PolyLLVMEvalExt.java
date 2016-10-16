package polyllvm.extension;

import polyglot.ast.Eval;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMEvalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LLVMNode translation = v.getTranslation(((Eval) node()).expr());
        if (translation instanceof LLVMESeq) {
            v.addTranslation(node(), ((LLVMESeq) translation).instruction());
        }
        else {
            v.addTranslation(node(), translation);
        }
        return super.translatePseudoLLVM(v);
    }
}
