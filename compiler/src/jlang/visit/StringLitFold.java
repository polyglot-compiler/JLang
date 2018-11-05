package jlang.visit;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

/**
 * Created by dantech on 11/4/18.
 */
public class StringLitFold extends NodeVisitor {

    protected TypeSystem ts;
    protected NodeFactory nf;

    public StringLitFold(TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v_) {
        if (!(n instanceof Expr)) {
            return n;
        }

        Expr e = (Expr) n;

        if (!lang().isConstant(e, lang())) {
            return e;
        }

        if (e instanceof Binary) {
            Binary b = (Binary) e;

            if (b.operator() == Binary.ADD
                    && lang().constantValue(b.left(), lang()) instanceof String
                    && lang().constantValue(b.right(), lang()) instanceof String) {
                String lit = (String) lang().constantValue(b, lang());
                Position pos = e.position();
                return nf.StringLit(pos, lit).type(ts.String());
            }
        }

        return e;
    }

}
