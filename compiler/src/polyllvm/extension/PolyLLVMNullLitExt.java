package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMNullLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        NullLit n = (NullLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypeNode type = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        v.addTranslation(n, nf.LLVMNullLiteral(type));
        return super.translatePseudoLLVM(v);
    }
}
