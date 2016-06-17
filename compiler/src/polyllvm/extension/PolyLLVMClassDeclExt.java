package polyllvm.extension;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        v.setCurrentClass((ClassDecl) node());
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        v.addTranslation(n, v.getTranslation(n.body()));
        v.clearCurrentClass();
        return super.translatePseudoLLVM(v);
    }

}
