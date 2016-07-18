package polyllvm.visit;

import java.util.ArrayDeque;
import java.util.Deque;

import polyglot.ast.MethodDecl;
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
    private Deque<MethodDecl> methods;

    public AddPrimitiveWideningCastsVisitor(PolyLLVMNodeFactory nf,
            TypeSystem ts) {
        super(nf.lang());
        this.nf = nf;
        this.ts = ts;
        methods = new ArrayDeque<>();
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public NodeVisitor enter(Node n) {
        return lang().enterAddPrimitiveWideningCasts(n, this);
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

    /**
     * Remove the current method from the stack of methods being visited
     */
    public void popCurrentMethod() {
        methods.pop();
    }

    /**
     * Set {@code m} as the new current method
     */
    public void setCurrentMethod(MethodDecl m) {
        methods.push(m);
    }

    /**
     * Return the current method
     */
    public MethodDecl getCurrentMethod() {
        return methods.peek();
    }

}
