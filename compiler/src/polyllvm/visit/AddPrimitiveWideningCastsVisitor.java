package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;

/**
 * Adds explicit casts for primitive type conversions. This happens on
 * assignment, Method invocation, and numeric promotion (see Java Language
 * Specification Chapter 5).
 *
 * @author Daniel
 */
public class AddPrimitiveWideningCastsVisitor extends NodeVisitor {

    private PolyLLVMNodeFactory nf;
    private TypeSystem ts;

    public AddPrimitiveWideningCastsVisitor(PolyLLVMNodeFactory nf,
            TypeSystem ts) {
        super(nf.lang());
        this.nf = nf;
        this.ts = ts;
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().addPrimitiveWideningCasts(n, this);
    }

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    @Override
    public String toString() {
        return "AddPrimitiveWideningCastsVisitor";
    }

    public TypeSystem typeSystem() {
        return ts;
    }
}
