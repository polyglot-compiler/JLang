package polyllvm.extension;

import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMIntLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        IntLit n = (IntLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypeNode tn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        if (n.type().isLongOrLess()) {
            v.addTranslation(n,
                             nf.LLVMIntLiteral(tn,
                                               n.value()));
        }
        else if (n.type().isFloat()) {
            v.addTranslation(n, nf.LLVMFloatLiteral(tn, n.value()));
        }
        else if (n.type().isDouble()) {
            v.addTranslation(n, nf.LLVMDoubleLiteral(tn, n.value()));
        }
        return super.translatePseudoLLVM(v);
    }
}
