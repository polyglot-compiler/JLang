package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PrintVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public class PolyLLVMExt extends Ext_c implements PolyLLVMOps {
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

    <<<<<<<HEAD

    @Override
    public Node print(PrintVisitor v) {
        System.out.println("NODE: " + node());
        return node();
    }

    @Override
    public Node removeStringLiterals(StringLiteralRemover v) {
        return node();
    }

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        return v;
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        return node();
    }

    @Override
    public Node addVoidReturn(AddVoidReturnVisitor v) {
        return node();
    }

    =======
    // TODO:  Override operation methods for overridden AST operations.
    >>>>>>>92481ea 628 bc4270d08ad51a6decb42ec21671b3
}
