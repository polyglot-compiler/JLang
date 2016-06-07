package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;

public class PolyLLVMLang_c extends JLang_c implements PolyLLVMLang {
    public static final PolyLLVMLang_c instance = new PolyLLVMLang_c();

    public static PolyLLVMLang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof PolyLLVMLang) return (PolyLLVMLang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected PolyLLVMLang_c() {
    }

    protected static PolyLLVMExt PolyLLVMExt(Node n) {
        return PolyLLVMExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return PolyLLVMExt(n);
    }

    // TODO:  Implement dispatch methods for new AST operations.
    // TODO:  Override *Ops methods for AST nodes with new extension nodes.
}
