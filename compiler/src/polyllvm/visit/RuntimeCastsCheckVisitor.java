package polyllvm.visit;

import polyglot.ast.*;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.ESeq;
import polyllvm.ast.PolyLLVMNodeFactory;

import java.util.ArrayList;

/**
 * Converts string literals to constructor calls, makes string concatenation explicit,
 * and promotes the corresponding concatenation arguments to strings.
 */
public class RuntimeCastsCheckVisitor extends NodeVisitor {
    private TypeSystem ts;
    private PolyLLVMNodeFactory nf;

    public RuntimeCastsCheckVisitor(TypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        Position pos = n.position();
        if (n instanceof Cast) {
            Cast c = (Cast) n;
            if(c.castType().type().isReference() && !ts.isImplicitCastValid(c.expr().type(), c.castType().type())){
                Expr instanceOfCheck = nf.Instanceof(pos, c.expr(), c.castType()).type(ts.Boolean());

                Expr classCastException = nf.New(pos, nf.CanonicalTypeNode(pos, ts.ClassCastException()), new ArrayList<>()).type(ts.ClassCastException());
                Throw throwClassCast = nf.Throw(pos, classCastException);
                Expr eSeq = nf.ESeq(throwClassCast, c).type(c.castType().type());

                return nf.Conditional(pos, instanceOfCheck, c, eSeq).type(c.castType().type());
            }
        }
        return super.leave(old, n, v);
    }
}
