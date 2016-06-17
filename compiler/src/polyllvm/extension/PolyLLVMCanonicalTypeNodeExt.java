package polyllvm.extension;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCanonicalTypeNodeExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        CanonicalTypeNode n = (CanonicalTypeNode) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypeNode t = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        v.addTranslation(n, t);
//        if (n.type().isByte()) {
//            v.addTranslation(node(),
//                             nf.LLVMIntType(Position.compilerGenerated(),
//                                            8,
//                                            null));
//        }
//        else if (n.type().isChar() || n.type().isShort()) {
//            v.addTranslation(node(),
//                             nf.LLVMIntType(Position.compilerGenerated(),
//                                            16,
//                                            null));
//        }
//        else if (n.type().isInt()) {
//            v.addTranslation(node(),
//                             nf.LLVMIntType(Position.compilerGenerated(),
//                                            32,
//                                            null));
//        }
//        else if (n.type().isLong()) {
//            v.addTranslation(node(),
//                             nf.LLVMIntType(Position.compilerGenerated(),
//                                            64,
//                                            null));
//        }
//        else if (n.type().isVoid()) {
//            v.addTranslation(node(),
//                             nf.LLVMVoidType(Position.compilerGenerated()));
//        }
//        else {
//            System.out.println(n);
//        }
        return super.translatePseudoLLVM(v);
    }
}
