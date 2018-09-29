//Copyright (C) 2018 Cornell University

package jlang.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.util.InternalCompilerError;
import polyglot.util.OptimalCodeWriter;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.PrettyPrinter;

import java.util.HashSet;
import java.util.Set;

import jlang.ast.JLangLang;
import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;
import jlang.util.TypedNodeFactory;

/**
 * Applies local desugar transformations by calling the
 * {@link jlang.ast.JLangOps#desugar(DesugarLocally)} method
 * on each node until a fixed point is reached.
 *
 * Reaching a fixed point allows desugar transformations to create nodes that
 * may need further desugaring---an important convenience to have. It also
 * frees us from worrying about the particular order in which we apply desugar
 * transformations, which in turn makes it easier to compose extensions.
 *
 * One way to reach a fixed point is to implement each desugar transformation
 * as a visitor pass, and then repeatedly run all passes until the AST stops
 * changing. Instead, we apply all desugar transformations in a single
 * visitor pass. To handle the possibility of a desugar transformation creating
 * new children to visit, we recurse into the children after every transformation.
 * The pass is kept efficient, however, by caching subtrees which have already been
 * fully desugared. In addition to this efficiency benefit, using a single
 * pass makes it more convenient to implement a desugar transformation,
 * since one does not have to create and schedule a standalone pass for each one.
 *
 * As part of this visitor, we also convert children to the type that their
 * parent expects. This is done in tandem with desugar transformations
 * because it is convenient to let desugar transformations both (1) assume
 * that their children are already type-converted, and (2) create new nodes
 * that have yet to be type-converted. (Put another way, we can model implicit
 * type conversions as a normal desugar transformation that happens to apply
 * to almost all expressions.) This is why we extend {@link AscriptionVisitor}.
 *
 * An example of a local desugar transformation is for array accesses:
 * {@code a[i]} can be desugared to an {@link jlang.ast.ESeq} of the form
 * {@code if (i < 0 || a >= a.length) throw new ClassCastException(); a[i]}.
 * In this case, the generated expression {@code a[i]} must be marked to indicate that
 * it is already guarded by array index check---otherwise we would desugar infinitely.
 *
 * Note that more involved desugar transformations which require non-local
 * information (such as {@link DesugarLocalClasses}) still need their own
 * standalone visitor pass.
 */
public class DesugarLocally extends DesugarImplicitConversions {
    public final Job job;
    public final JLangTypeSystem ts;
    public final JLangNodeFactory nf;
    public final TypedNodeFactory tnf;

    /**
     * Caches nodes that have already been visited so that our traversal
     * (which can recurse into children multiple times) remains efficient.
     */
    protected Set<Node> desugared = new HashSet<>();

    public DesugarLocally(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public JLangLang lang() {
        return (JLangLang) super.lang();
    }

    /** Desugar until fixed point. */
    @Override
    public Node override(Node parent, Node n) {
        // Check for type conversion.
        // We do this before checking whether the current node is already desugared,
        // because a type conversion might be needed even if only the parent is unstable.
        DesugarLocally entered = (DesugarLocally) enter(parent, n);
        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (entered.toType() == null)
                throw new InternalCompilerError(
                        "Null expected type for " + n.getClass() + " with parent " + parent);
            n = convertType(parent, e, entered.toType());
        }

        // If already desugared this subtree, no work to do.
        if (desugared.contains(n))
            return n;

        // Small hack to allow ESeq nodes on the left side of an assignment.
        if (n instanceof LocalAssign
                || n instanceof FieldAssign
                || n instanceof ArrayAccessAssign) {
            Assign a = (Assign) n;
            n = nf.AmbAssign(a.position(), a.left(), a.operator(), a.right()).type(a.type());
        }

        // Desugar until fixed point.
        // Notice that each desugar transformation can assume the children are desugared.
        int count = 0;
        while (true) {
            if (++count > 1000) {
                System.err.println("Desugar transformations should not take nearly this long");
                System.err.println("Printing the current state of the node to be desugared.");
                new PrettyPrinter(lang()).printAst(n, new OptimalCodeWriter(System.err, 120));
                System.err.println();
                throw new InternalCompilerError("Aborting due to failed desugar transformation.");
            }

            n = lang().visitChildren(n, entered);

            Node prev = n;
            n = lang().desugar(n, entered);
            if (n == prev) {
                break;
            }
        }

        desugared.add(n);
        return n;
    }
}
