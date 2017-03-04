package polyllvm.extension;

import polyglot.ast.Initializer;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMInitializerExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        Initializer n = (Initializer) node();
        if (n.flags().isStatic()) {
            v.addCtor(() -> {
                n.body().visit(v);
                return null;
            });
        } else {
            // TODO: Non-static initializers should run just before constructor code.
            throw new InternalCompilerError("Non-static initializers not implemented");
        }
        return n;
    }
}
