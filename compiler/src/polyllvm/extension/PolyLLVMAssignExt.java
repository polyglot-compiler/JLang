package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        Assign n = (Assign) node();
        if (!n.operator().equals(Assign.ASSIGN))
            throw new InternalCompilerError("Non-vanilla assignments should be de-sugared");
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Assign n = (Assign) node();
        Variable target = (Variable) n.left();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Expr expr = n.right();

        if (target.type().equals(expr.type())) {
            return super.addPrimitiveWideningCasts(v);
        }

        if (target.type().isPrimitive() && expr.type().isPrimitive()) {
            CanonicalTypeNode ctn =
                    nf.CanonicalTypeNode(Position.compilerGenerated(),
                                         target.type());
            Expr cast = nf.Cast(Position.compilerGenerated(), ctn, expr)
                          .type(target.type());
            return n.right(cast);
        }
        else if (!target.type().isPrimitive() && !expr.type().isPrimitive()) {
            CanonicalTypeNode ctn =
                    nf.CanonicalTypeNode(Position.compilerGenerated(),
                                         target.type());
            Expr cast = nf.Cast(Position.compilerGenerated(), ctn, expr)
                          .type(target.type());
            return n.right(cast);

        }

        return super.addPrimitiveWideningCasts(v);
    }

}
