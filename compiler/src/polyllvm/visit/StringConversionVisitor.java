package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyllvm.util.Constants;

import java.util.Collections;

/**
 * Makes string concatenation explicit and promotes the corresponding concatenation
 * arguments to strings. Preserves typing.
 */
public class StringConversionVisitor extends ContextVisitor {

    public StringConversionVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        Position pos = n.position();
        if (n instanceof Binary) {
            // String conversions due to concatenation.
            Binary binary = (Binary) n;
            Expr l = binary.left(), r = binary.right();
            Type lt = l.type(), rt = r.type();
            if (lt.typeEquals(ts.String()) || rt.typeEquals(ts.String())) {
                if (binary.operator().equals(Binary.ADD)) {
                    l = convertToString(l);
                    r = convertToString(r);

                    // Call String.concat(...)
                    return nf.Call(pos, l, nf.Id(pos, "concat"), r)
                            .methodInstance(ts.findMethod(
                                    ts.String(),
                                    "concat",
                                    Collections.singletonList(ts.String()),
                                    context().currentClass(),
                                    /*fromClient*/ true))
                            .type(ts.String());
                }
            }
        }
        return super.leave(old, n, v);
    }

    private Expr convertToString(Expr e) throws SemanticException {
        Type t = e.type();
        Position pos = e.position();

        if (t.isNull()) {
            // Substitute "null".
            return nf.StringLit(pos, "null").type(ts.String());
        }
        else if (t.isPrimitive()) {
            // Call String.valueOf(...)
            return nf.Call(pos, nf.CanonicalTypeNode(pos, ts.String()), nf.Id(pos, "valueOf"), e)
                    .methodInstance(ts.findMethod(
                            ts.String(),
                            "valueOf",
                            Collections.singletonList(e.type()),
                            context().currentClass(),
                            /*fromClient*/ true))
                    .type(ts.String());
        }
        else {
            assert t.isReference();

            // Call toString(...) in the runtime library, which will have the right semantics
            // if e has a null value or if e.toString() has a null value.
            Type helperType = ts.typeForName(Constants.RUNTIME_HELPER).toReference();
            Receiver helperReceiver = nf.CanonicalTypeNode(pos, helperType);
            return nf.Call(pos, helperReceiver, nf.Id(pos, "toString"), e)
                    .methodInstance(ts.findMethod(
                            helperType.toReference(),
                            "toString",
                            Collections.singletonList(ts.Object()),
                            helperType.toClass(), // Lie to be able to access runtime helper.
                            /*fromClient*/ true))
                    .type(ts.String());
        }
    }
}
