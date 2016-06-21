package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMTypeNodeExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        TypeNode n = (TypeNode) node();

        return super.translatePseudoLLVM(v);
    }
}
