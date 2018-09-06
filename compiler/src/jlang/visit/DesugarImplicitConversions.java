//Copyright (C) 2018 Cornell University

package jlang.visit;

import jlang.ast.JLangExt;
import jlang.ast.JLangNodeFactory;
import jlang.extension.JLangCastExt;
import jlang.extension.JLangCastExt.ConversionContext;
import jlang.types.JLangTypeSystem;
import jlang.util.TypedNodeFactory;
import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Turn implicit casts and type promotions into explicit casts.
 * Example: `char c = 1` becomes `char c = (char) 1`.
 * Example: `1 + 2l` becomes `((long) 1) + 2l`.
 *
 * Preserves typing, but mutates array initializer expressions to directly have the
 * type that its parent expects.
 */
public class DesugarImplicitConversions extends AscriptionVisitor {
    private final TypedNodeFactory tnf;

    public DesugarImplicitConversions(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
        this.tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public JLangTypeSystem typeSystem() {
        return (JLangTypeSystem) super.typeSystem();
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v) {

        if (n instanceof Expr) {
            Expr e = (Expr) n;
            Type type = ((AscriptionVisitor) v).toType();
            if (type == null)
                throw new InternalCompilerError(
                        "Null expected type for " + n.getClass() + " with parent " + parent);
            return convertType(parent, e, type);
        }

        return n;
    }

    protected Expr convertType(Node parent, Expr e, Type toType) {
        TypeSystem ts = typeSystem();

        if (toType.isVoid()) {
            // No cast necessary.
            return e;
        }

        if (parent instanceof Cast) {
            // Already an explicit cast here.
            return e;
        }

        if (e instanceof ArrayInit) {
            // We change the types of array initializer expressions directly (rather than using
            // a cast) because (1) the correct element type is needed during translation to create
            // correct allocation code, and (2) Polyglot does not allow the initializer expression
            // of a NewArray node to be a cast.
            return e.type(ts.arrayOf(toType.toArray().base()));
        }

        ConversionContext context = computeConversionContext(parent, toType);

        if (!context.equals(ConversionContext.STRING_CONCAT)
                && typeSystem().typeEqualsErased(e.type(), toType)) {
            // Avoid adding redundant casts.
            return e;
        }

        Cast cast = tnf.Cast(e, toType);
        JLangCastExt ext = (JLangCastExt) JLangExt.ext(cast);
        return ext.context(context);
    }

    /** Determine the conversion context (a JLS concept) based on the parent node. */
    protected ConversionContext computeConversionContext(Node parent, Type toType) {
        if (parent instanceof ProcedureCall) {
            return ConversionContext.METHOD_INVOCATION;
        }
        else if (parent instanceof Binary
                && ((Binary) parent).operator().equals(Binary.ADD)
                && toType.typeEquals(ts.String())) {
            return ConversionContext.STRING_CONCAT;
        }
        else if (parent instanceof Binary || parent instanceof Unary) {
            return ConversionContext.NUMERIC_PROMOTION;
        }
        else {
            // Assume all others are assignment conversions.
            return ConversionContext.ASSIGNMENT;
        }
    }
}
