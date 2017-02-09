package polyllvm.extension;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCanonicalTypeNodeExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        CanonicalTypeNode n = (CanonicalTypeNode) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypeNode t = LLVMUtils.polyLLVMTypeNode(nf, n.type());
        v.addTranslation(n, t);
        if (n.type().isReference()) {
            v.addClassType((ReferenceType) n.type());
        }
        return super.translatePseudoLLVM(v);
    }
}
