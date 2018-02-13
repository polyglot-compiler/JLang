package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyllvm.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces assert statements with a check and a throw.
 * Preserves typing.
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

        List<Type> argTypes = new ArrayList<>();
        List<Expr> args = new ArrayList<>();
        argTypes.add(ts.Boolean());
        args.add(a.cond());
        if (a.errorMessage() != null) {
            argTypes.add(ts.String());
            args.add(a.errorMessage());
        }

        Type helperType = ts.typeForName(Constants.RUNTIME_HELPER).toReference();
        Receiver helperReceiver = nf.CanonicalTypeNode(pos, helperType);
        return nf.Call(pos, helperReceiver, nf.Id(pos, "assertHelper"), args)
                .methodInstance(ts.findMethod(
                        helperType.toReference(),
                        "assertHelper",
                        argTypes,
                        helperType.toClass(), // Lie to be able to access runtime helper.
                        /*fromClient*/ true))
                .type(ts.String());
    }
}
