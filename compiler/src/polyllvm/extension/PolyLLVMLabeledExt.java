package polyllvm.extension;

import polyglot.ast.Labeled;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLabeledExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        Labeled n = (Labeled) node();
        if (n.statement() instanceof Loop) {
            v.enterLabeled(n);
        }
        return super.enterTranslatePseudoLLVM(v);
    }
}
