package polyllvm.visit;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.Constants;

/**
 * Makes string concatenation explicit and promotes the corresponding concatenation
 * arguments to strings. Preserves typing.
 */
public class DesugarStringConcatenation extends DesugarVisitor {

    public DesugarStringConcatenation(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {
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
        return super.leaveDesugar(n);
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
