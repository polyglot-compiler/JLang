package polyllvm.visit;

import polyglot.ast.Assert;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyllvm.util.Constants;
import polyllvm.util.TypedNodeFactory;

/**
 * Replaces assert statements with a check and a throw.
 * Preserves typing.
 */
public class DesugarAsserts extends ContextVisitor {

    private final TypedNodeFactory tnf;

    public DesugarAsserts(Job job, JL5TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        tnf = new TypedNodeFactory(ts, nf);
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

        Expr[] args = a.errorMessage() == null
                ? new Expr[] { a.cond() }
                : new Expr[] { a.cond(), a.errorMessage() };

        ClassType helper = ts.typeForName(Constants.RUNTIME_HELPER).toClass();
        return tnf.StaticCall(pos, "assertHelper", helper, ts.Void(), args);
    }
}
