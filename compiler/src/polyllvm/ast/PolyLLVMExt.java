package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class PolyLLVMExt extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static PolyLLVMExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof PolyLLVMExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No PolyLLVM extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (PolyLLVMExt) e;
    }

    @Override
    public final PolyLLVMLang lang() {
        return PolyLLVMLang_c.instance;
    }

    // TODO:  Override operation methods for overridden AST operations.
}
