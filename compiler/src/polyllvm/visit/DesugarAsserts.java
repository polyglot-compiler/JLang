package polyllvm.visit;

import polyglot.ast.Assert;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.Constants;

/**
 * Replaces assert statements with a check and a throw.
 * Preserves typing.
 */
public class DesugarAsserts extends DesugarVisitor {

    public DesugarAsserts(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    public Node leaveDesugar(Node n) throws SemanticException {
        if (!(n instanceof Assert))
            return super.leaveDesugar(n);

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
