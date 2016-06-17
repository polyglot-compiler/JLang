package polyllvm.extension;

import polyglot.ast.Eval;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMEvalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        v.addTranslation(node(), v.getTranslation(((Eval) node()).expr()));
        return super.translatePseudoLLVM(v);
    }
}
