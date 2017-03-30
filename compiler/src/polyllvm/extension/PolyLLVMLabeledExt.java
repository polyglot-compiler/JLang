package polyllvm.extension;

import polyglot.ast.Labeled;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

//TODO: rewrite this to be better!
public class PolyLLVMLabeledExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        return super.overrideTranslatePseudoLLVM(v);
    }

    @Override
    public LLVMTranslator enterTranslatePseudoLLVM(
            LLVMTranslator v) {
        Labeled n = (Labeled) node();
        v.enterLabeled(n);
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        v.exitLabeled((Labeled) node());
        return super.translatePseudoLLVM(v);
    }
}
