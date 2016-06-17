package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;

/**
 * Remove string literals by replacing them with an equivalent constructor
 * which takes a byte array of the string data.
 * @author Daniel
 *
 */
public class StringLiteralRemover extends NodeVisitor {

    private NodeFactory nf;
    private TypeSystem ts;

    public StringLiteralRemover(TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;

    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().removeStringLiterals(n, this);
    }

    public NodeFactory nodeFactory() {
        return nf;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

}
