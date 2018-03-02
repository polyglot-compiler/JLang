package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;

/**
 * Applies desugar transformations to nodes by calling their
 * {@link polyllvm.ast.PolyLLVMOps#desugar(DesugarLocally)} method
 * until a fixed point is reached.
 *
 * These desugar transformations can only rely on information available to each node
 * in isolation. In return for this constraint, we can be sure that desugar
 * transformations compose well, and we can apply them all in one visitor pass.
 * More involved desugar transformations (such as {@link DesugarLocalClasses})
 * need their own visitors.
 *
 * A great example is array accesses: {@code a[i]} can be desugared to an
 * {@link polyllvm.ast.ESeq} of the form
 * {@code if (i < 0 || a >= a.length) then throw new ClassCastException else a[i]}
 */
public class DesugarLocally extends NodeVisitor {
    public final Job job;
    public final PolyLLVMTypeSystem ts;
    public final PolyLLVMNodeFactory nf;
    public final TypedNodeFactory tnf;

    public DesugarLocally(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Node> N visitEdge(Node parent, N child) {
        // Desugar until fixed point, then recurse to children.
        int count = 0;
        Node prev;
        do {
            if (++count > 1000)
                throw new InternalCompilerError(
                        "Desugar transformations should not take nearly this long to reach a " +
                                "fixed point; is this not a finite lattice?");
            prev = child;
            child = (N) lang().desugar(child, this);
        } while (child != prev);
        return super.visitEdge(parent, child);
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (lang().desugar(n, this) != n)
            throw new InternalCompilerError(
                    "The desugar fixed point reached for this node should not be " +
                            "affected by desugaring its children");
        return super.leave(old, n, v);
    }
}
