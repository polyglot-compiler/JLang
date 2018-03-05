package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.util.InternalCompilerError;
import polyglot.util.OptimalCodeWriter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;

/**
 * Applies desugar transformations to nodes by calling their
 * {@link polyllvm.ast.PolyLLVMOps#desugar(DesugarLocally)} method
 * until a fixed point is reached.
 *
 * These desugar transformations can only rely on information available to the node
 * in isolation, and nodes cannot assume that their children have been desugared.
 * In return for these constraint, we can be sure that desugar
 * transformations compose well, and we can apply them all in one visitor pass.
 * This is not just for efficiency; it frees us from worrying about the particular
 * order in which we apply simple desugar transformations, and it makes it
 * easy for extensions for override desugar transformations for particular nodes.
 *
 * More involved desugar transformations (such as {@link DesugarLocalClasses})
 * need their own visitors.
 *
 * One example is array accesses: {@code a[i]} can be desugared to an
 * {@link polyllvm.ast.ESeq} of the form
 * {@code if (i < 0 || a >= a.length) then throw new ClassCastException() else a[i]}.
 * In this case, the generated expression {@code a[i]} must be marked to indicate that
 * it is now guarded by array index check---otherwise we would desugar infinitely.
 */
public class DesugarLocally extends NodeVisitor {
    public final Job job;
    public final PolyLLVMTypeSystem ts;
    public final PolyLLVMNodeFactory nf;
    public final TypedNodeFactory tnf;

    /**
     * This is an {@link polyglot.visit.AscriptionVisitor} that uses the child expected
     * type of a node to add explicit casts where an implicit conversion takes place.
     * We intertwine its traversal of the AST with this one since we model
     * implicit conversions as a desugar transformation.
     */
    protected DesugarImplicitConversions implicitConversions;

    public DesugarLocally(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.tnf = new TypedNodeFactory(ts, nf);
        implicitConversions = new DesugarImplicitConversions(job, ts, nf);
    }



    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public NodeVisitor begin() {
        implicitConversions = (DesugarImplicitConversions) implicitConversions.begin();
        return super.begin();
    }

    /**
     * Desugar until fixed point.
     * The parent has already been desugared, but the children of this node have not.
     */
    @Override
    public Node override(Node parent, Node n) {
        DesugarImplicitConversions prevImplicitConversions = implicitConversions;
        implicitConversions = (DesugarImplicitConversions) prevImplicitConversions.enter(parent, n);

        // Desugar until fixed point.
        int count = 0;
        while (true) {
            if (++count > 1000) {
                System.err.println(
                        "Desugar transformations should not take nearly this long to reach a " +
                                "fixed point.\nIs this not a finite lattice?");
                System.err.println("Printing the current state of the node to be desugared.");
                new PrettyPrinter(lang()).printAst(n, new OptimalCodeWriter(System.err, 120));
                System.err.println();
                throw new InternalCompilerError("Aborting due to failed desugar transformation.");
            }

            Node prev = n;

            // We model implicit conversions as a desugar transformation.
            //
            // We want to allow desugar transformation to assume that their children
            // are already converted to their expected type, yet we also want to allow
            // desugar transformations to rewrite nodes in a way that (safely) changes the
            // expected type of a child. (Consider, for example, rewriting a node into
            // a runtime library call that takes in a generic Object argument.) Thus we
            // convert all children to the correct type on every iteration.
            n = lang().visitChildren(n, new NodeVisitor(nf.lang()) {
                @Override
                public Node override(Node parent, Node n) {
                    // We explicitly avoid recursion here since we only care about
                    // converting the immediate children.
                    DesugarImplicitConversions entered =
                            (DesugarImplicitConversions) implicitConversions.enter(parent, n);
                    return implicitConversions.leave(parent, n, n, entered);
                }
            });

            n = lang().desugar(n, this);
            if (n == prev) {
                break;
            }
        }

        // Recurse to children.
        n = lang().visitChildren(n, this);

        // Check that desugar transformations do not depend on their children being desugared.
        if (lang().desugar(n, this) != n)
            throw new InternalCompilerError(
                    "The desugar fixed point reached for this node should not be " +
                            "affected by desugaring its children");

        implicitConversions = prevImplicitConversions;
        return n;
    }
}
