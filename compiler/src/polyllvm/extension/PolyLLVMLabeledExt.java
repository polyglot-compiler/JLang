package polyllvm.extension;

import polyglot.ast.Labeled;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMLabeledExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LLVMTranslator enterTranslatePseudoLLVM(
            LLVMTranslator v) {
        Labeled n = (Labeled) node();
        v.pushLoopLabel(n.label());
        return super.enterTranslatePseudoLLVM(v);
    }
}
