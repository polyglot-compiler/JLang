package polyllvm.visit;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

/**
 * Converts string literals to constructor calls, makes string concatenation explicit,
 * and promotes the corresponding concatenation arguments to strings.
 */
public class StringConversionVisitor extends NodeVisitor {
    private TypeSystem ts;
    private NodeFactory nf;

    public StringConversionVisitor(TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        Position pos = n.position();
        if (n instanceof Binary) {
            Binary binary = (Binary) n;
            Expr l = binary.left(), r = binary.right();
            Type lt = l.type(), rt = r.type();
            if (lt.typeEquals(ts.String()) || rt.typeEquals(ts.String())) {
                if (binary.operator().equals(Binary.ADD)) {
                    l = convertToString(l);
                    r = convertToString(r);
                    return nf.Call(pos, l, nf.Id(pos, "concat"), r).type(ts.String());
                }
            }
        }
        return super.leave(old, n, v);
    }

    private Expr convertToString(Expr e) {
        Type t = e.type();
        Position pos = e.position();
        if (t.typeEquals(ts.String())) {
            return e;
        }
        else if (t.isNull()) {
            return nf.StringLit(pos, "null").type(ts.String());
        }
        else if (t.isPrimitive()) {
            return nf.Call(pos, nf.CanonicalTypeNode(pos, ts.String()), nf.Id(pos, "valueOf"), e)
                    .type(ts.String());
        }
        else {
            // TODO: According to the JLS, technically want "null" if toString() returns null.
            assert t.isReference();
            return nf.Call(pos, e, nf.Id(pos, "toString")).type(ts.String());
        }
    }
}
