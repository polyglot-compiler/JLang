package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.lex.StringLiteral;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts string literals to constructor calls, makes string concatenation explicit,
 * and promotes the corresponding concatenation arguments to strings.
 */
public class StringConversionVisitor extends ContextVisitor {

    public StringConversionVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        Position pos = Position.COMPILER_GENERATED;
        if (n instanceof StringLiteral) {
            // TODO: Optimize by pre-creating byte array.
            StringLit str = (StringLit) n;
            List<Expr> byteExprs = new ArrayList<>();
            char bytes[] = str.value().toCharArray();
            for (char b : bytes)
                byteExprs.add(nf.CharLit(pos, b));
            TypeNode byteType = nf.CanonicalTypeNode(pos, ts.Byte());
            Expr byteArray = nf.NewArray(pos, byteType, /* dims */ 1, nf.ArrayInit(pos, byteExprs));
            TypeNode strType = nf.CanonicalTypeNode(pos, ts.String());
            return nf.New(pos, strType, Collections.singletonList(byteArray));
        }
        else if (n instanceof Binary) {
            Binary binary = (Binary) n;
            Expr l = binary.left(), r = binary.right();
            Type lt = l.type(), rt = r.type();
            if (lt.typeEquals(ts.String()) || rt.typeEquals(ts.String())) {
                assert binary.operator().equals(Binary.ADD);
                return nf.Call(pos, convertToString(l), nf.Id(pos, "concat"), convertToString(r));
            }
        }
        return super.leave(old, n, v);
    }

    private Expr convertToString(Expr e) {
        Type t = e.type();
        Position pos = Position.COMPILER_GENERATED;
        if (t.typeEquals(ts.String())) {
            return e;
        }
        else if (t.isNull()) {
            return nf.StringLit(pos, "null");
        }
        else if (t.isPrimitive()) {
            ClassLit stringClass = nf.ClassLit(pos, nf.CanonicalTypeNode(pos, ts.String()));
            return nf.Call(pos, stringClass, nf.Id(pos, "valueOf"), e);
        }
        else {
            // TODO: According to the JLS, technically want "null" if toString() returns null.
            assert t.isReference();
            return nf.Call(pos, e, nf.Id(pos, "toString"));
        }
    }
}
