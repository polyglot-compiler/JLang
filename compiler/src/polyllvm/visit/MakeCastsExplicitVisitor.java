package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Turn (almost) all implicit casts and type promotions into explicit casts.
 * <p>
 * Examples:
 * <p>
 * <table border="1">
 * <tr>
 * <th>Before</th>
 * <th>After</th>
 * </tr>
 * <tr>
 * <td>{@code char c = 1}</td>
 * <td>{@code char c = (char) 1}</td>
 * </tr>
 * <tr>
 * <td>{@code 1 + 2l}</td>
 * <td>{@code ((long) 1) + 2l}</td>
 * </tr>
 * </table>
 */
public class MakeCastsExplicitVisitor extends AscriptionVisitor {

    public MakeCastsExplicitVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (parent instanceof Cast) {
            // Avoid redundant casts.
            return n;
        } else if (n instanceof Field && parent instanceof FieldAssign) {
            // The LHS of a FieldAssign cannot be a Cast. Plus there is no need
            // to cast the LHS anyway.
            return n;
        } else {
            return super.leaveCall(parent, old, n, v);
        }
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        if (e.type().typeEquals(toType) || toType.isVoid()) {
            // Avoid redundant casts.
            return super.ascribe(e, toType);
        }
        else if (e instanceof ArrayInit) {
            // We change the types of array initializer expressions directly (rather than using
            // a cast) because (1) the correct element type is needed during translation to create
            // correct allocation code, and (2) Polyglot does not allow the initializer expression
            // of a NewArray node to be a cast.
            return e.type(typeSystem().arrayOf(toType.toArray().base()));
        }
        else {
            // Add cast.
            NodeFactory nf = nodeFactory();
            Position pos = e.position();
            TypeNode typeNode = nf.CanonicalTypeNode(pos, toType);
            return nf.Cast(pos, typeNode, e).type(toType);
        }
    }

}
