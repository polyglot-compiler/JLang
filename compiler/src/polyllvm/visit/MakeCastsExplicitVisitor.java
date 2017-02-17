package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Turn all implicit casts and type promotions into explicit casts.
 * Examples:
 * {@code char c = 1} -> {@code char c = (char) 1}.
 * {@code 1 + 2l} -> {@code ((long) 1) + 2l}.
 */
public class MakeCastsExplicitVisitor extends AscriptionVisitor {

    public MakeCastsExplicitVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v) throws SemanticException {
        if (parent instanceof Cast) {
            // We already have a cast; no need to add another.
            return n;
        } else if (parent instanceof Eval) {
            // We don't want to cast the eval sub-expression to void.
            return n;
        } else {
            return super.leaveCall(parent, old, n, v);
        }
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        // Avoid adding casts to void.
        if (e.type().typeEquals(toType) || toType.isVoid()) {
            return super.ascribe(e, toType);
        } else {
            NodeFactory nf = nodeFactory();
            Position pos = Position.COMPILER_GENERATED;
            TypeNode typeNode = nf.CanonicalTypeNode(pos, toType);
            return nf.Cast(pos, typeNode, e).type(toType);
        }
    }
}
