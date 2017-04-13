package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;

import java.util.Collections;
import java.util.List;

/**
 * Replaces assert statements with a check and a throw.
 */
public class AssertionVisitor extends ContextVisitor {

    public AssertionVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        if (!(n instanceof Assert))
            return super.leaveCall(n);

        if (!Options.global.assertions) {
            // Assertions are disabled.
            return nf.Empty(n.position());
        }

        Assert a = (Assert) n;
        Position pos = n.position();

        List<Type> argTypes = a.errorMessage() == null
                ? Collections.emptyList()
                : Collections.singletonList(ts.String());
        List<Expr> args = a.errorMessage() == null
                ? Collections.emptyList()
                : Collections.singletonList(a.errorMessage());
        ConstructorInstance constructor = ts.findConstructor(
                ts.ClassCastException(),
                argTypes,
                context().currentClass(),
                /*fromClient*/ true);

        CanonicalTypeNode exceptionType = nf.CanonicalTypeNode(pos, ts.AssertionError());
        Expr error = nf.New(pos, exceptionType, args)
                .constructorInstance(constructor)
                .type(ts.AssertionError());
        Throw throwError = nf.Throw(pos, error);
        Expr cond = nf.Unary(pos, Unary.NOT, a.cond());
        return nf.If(pos, cond, throwError);
    }
}
