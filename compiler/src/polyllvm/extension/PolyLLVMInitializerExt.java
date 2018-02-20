package polyllvm.extension;

import polyglot.ast.Initializer;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMInitializerExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Initializer n = (Initializer) node();
        // Non-static initializers are handled by the constructors.
        if (n.flags().isStatic()) {
            v.utils.buildCtor(() -> {
                n.body().visit(v);
                return null;
            });
        }
        return n;
    }
}
