package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;

/** A visitor convenient for desugar passes. Rethrows semantic exceptions, for example. */
public abstract class DesugarVisitor extends NodeVisitor {
    protected Job job;
    protected PolyLLVMTypeSystem ts;
    protected PolyLLVMNodeFactory nf;
    protected TypedNodeFactory tnf;

    DesugarVisitor(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public final NodeVisitor enter(Node parent, Node n) {
        try {
            enterDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        return this;
    }

    @Override
    public final NodeVisitor enter(Node n) {
        throw new InternalCompilerError("Should not be called");
    }

    @Override
    public final Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        try {
            return leaveDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    @Override
    public final Node leave(Node old, Node n, NodeVisitor v) {
        throw new InternalCompilerError("Should not be called");
    }

    public NodeVisitor enterDesugar(Node parent, Node n) throws SemanticException {
        return enterDesugar(n);
    }

    public NodeVisitor enterDesugar(Node n) throws SemanticException {
        return this;
    }

    public Node leaveDesugar(Node parent, Node n) throws SemanticException {
        return leaveDesugar(n);
    }

    public Node leaveDesugar(Node n) throws SemanticException {
        return n;
    }
}
