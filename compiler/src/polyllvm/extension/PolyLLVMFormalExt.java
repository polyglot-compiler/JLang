package polyllvm.extension;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMFormalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Formal n = (Formal) node();
        LLVMTypeNode t = (LLVMTypeNode) v.getTranslation(n.type());
        LLVMArgDecl ad =
                v.nodeFactory().LLVMArgDecl(Position.compilerGenerated(),
                                            t,
                                            n.name());
        v.addTranslation(n, ad);
        return super.translatePseudoLLVM(v);
    }
}
