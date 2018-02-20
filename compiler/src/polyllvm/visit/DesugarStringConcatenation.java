package polyllvm.visit;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyllvm.util.Constants;
import polyllvm.util.TypedNodeFactory;

/**
 * Makes string concatenation explicit and promotes the corresponding concatenation
 * arguments to strings. Preserves typing.
 */
public class DesugarStringConcatenation extends ContextVisitor {

    private final TypedNodeFactory tnf;

    public DesugarStringConcatenation(Job job, JL5TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        tnf = new TypedNodeFactory(ts, nf);
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
                    // Call String.concat(...)
                    l = convertToString(l);
                    r = convertToString(r);
                    return tnf.Call(pos, l, "concat", ts.String(), ts.String(), r);
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
            return tnf.StaticCall(pos, "valueOf", ts.String(), ts.String(), e);
        }
        else {
            assert t.isReference();

            // Call toString(...) in the runtime library, which will have the right semantics
            // if e has a null value or if e.toString() has a null value.
            ClassType helperType = ts.typeForName(Constants.RUNTIME_HELPER).toClass();
            return tnf.StaticCall(pos, "toString", helperType, ts.String(), e);
        }
    }
}
