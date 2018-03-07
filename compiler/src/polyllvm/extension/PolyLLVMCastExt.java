package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.DesugarLocally;

import java.lang.Override;
import java.util.Arrays;
import java.util.Collections;

import static org.bytedeco.javacpp.LLVM.*;
import static polyllvm.extension.PolyLLVMCastExt.ConversionContext.*;

/**
 * We use cast nodes to model both explicit user-added casts and implicit conversions.
 * Read JLS SE 7, Chapter 5, Conversions and Promotions. Each subsection title
 * specifying a conversion (e.g., 5.1.1) is searchable in comments here.
 *
 * TODO: We are not kind where Java is kind (i.e., defined behavior for underflow/overflow/NaN).
 *
 * Note that Polyglot already implements the logic to decide
 * what the final converted type of an expression should be, but we must emit code
 * to actually perform the conversion(s) to get there. This is nontrivial.
 *
 * We use an {@link AscriptionVisitor} to add explicit casts where
 * Polyglot claims a conversion is necessary, and then we emit conversion
 * code when translating {@link Cast} nodes here.
 */
public class PolyLLVMCastExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** Conversion context, as specified by the JLS. */
    public enum ConversionContext {
        ASSIGNMENT,        // Implicit assignment conversion.
        METHOD_INVOCATION, // Implicit method invocation conversion.
        EXPLICIT_CAST,     // Explicit user-added cast.
        STRING_CONCAT,     // Implicit string conversion due to string concatenation.
        NUMERIC_PROMOTION, // Implicit numeric promotion due to numeric operator.
        GUARDED_BITCAST,   // Compiler-generated bitcast.
    }

    private ConversionContext context = EXPLICIT_CAST;

    public Cast context(ConversionContext context) {
        return context((Cast) node(), context);
    }

    public Cast context(Cast c, ConversionContext context) {
        PolyLLVMCastExt ext = (PolyLLVMCastExt) PolyLLVMExt.ext(c);
        if (ext.context == context) return c;
        if (c == node) {
            c = Copy.Util.copy(c);
            ext = (PolyLLVMCastExt) PolyLLVMExt.ext(c);
        }
        ext.context = context;
        return c;
    }

    /**
     * We desugar each cast into a *single* conversion,
     * and take care of a few of the conversions directly here.
     */
    @Override
    public Node desugar(DesugarLocally v) {
        Cast n = (Cast) node();
        Type from = n.expr().type();
        Type to = n.type();

        // 5.1.11 String Conversion.
        if (context.equals(STRING_CONCAT))
            return stringConversion(v, n.expr(), from);

        // 5.1.1 Identity Conversion.
        if (from.typeEquals(to))
            return n.expr();

        // 5.1.7 Boxing Conversion.
        if (from.isPrimitive() && to.isClass())
            return boxingConversion(v, n.expr(), from.toPrimitive(), to.toClass());

        // 5.1.8 Unboxing Conversion.
        if (from.isClass() && to.isPrimitive())
            return unboxingConversion(v, n.expr(), from.toClass(), to.toPrimitive());

        // 5.1.4 Widening and Narrowing Primitive Conversion.
        if (from.isByte() && to.isChar())
            return n.expr(v.tnf.Cast(n.expr(), v.ts.Int()));

        // 5.1.6 Narrowing Reference Conversion (guard with instanceof check).
        if (!context.equals(GUARDED_BITCAST)
                && from.isReference() && to.isReference() && !from.isSubtype(to))
            return narrowingReferenceConversion(v, n.expr(), from.toReference(), to.toReference());

        return super.desugar(v);
    }

    /** Implements the conversions we could not handle through desugaring. */
    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Cast n = (Cast) node();

        Type from = n.expr().type();
        Type to = n.castType().type();
        LLVMValueRef e = v.getTranslation(n.expr());
        LLVMTypeRef llT = v.utils.toLL(to);
        assert !from.typeEquals(to);

        LLVMValueRef res;
        if (from.isPrimitive() && to.isPrimitive()) {
            // 5.1.2 Widening Primitive Conversion.
            // 5.1.3 Narrowing Primitive Conversion.
            res = primitiveConversion(v, e, from.toPrimitive(), to.toPrimitive());
        }
        else if ((from.isReference() || from.isNull()) && to.isReference()) {
            // 5.1.5 Widening Reference Conversion.
            // 5.1.6 Narrowing Reference Conversion (already guarded by instanceof check).
            // 5.1.9 Unchecked Conversion (irrelevant after erasure).
            // 5.1.10 Capture Conversion (irrelevant after erasure).
            assert from.isSubtype(to) || context.equals(GUARDED_BITCAST);
            res = LLVMBuildBitCast(v.builder, e, llT, "cast");
        }
        else {
            throw new InternalCompilerError("Unhandled cast from " + from + " to " + to);
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    /** Convert primitive value to boxed value. */
    protected Node boxingConversion(
            DesugarLocally v, Expr e, PrimitiveType from, ClassType to) {

        Position pos = e.position();
        if (v.ts.isPrimitiveWrapper(to)) {
            // Convert directly to wrapper type.
            // This may be preceded by a primitive narrowing conversion (e.g., `Byte b = 1;`).
            Expr primCast = v.tnf.Cast(e, v.ts.primitiveTypeOfWrapper(to));
            return v.tnf.StaticCall(pos, "valueOf", to, to, primCast);
        } else {
            // Convert to wrapper type.
            // This may be followed by a widening reference conversion.
            ClassType wrapType = v.ts.wrapperClassOfPrimitive(from);
            Call wrap = v.tnf.StaticCall(pos, "valueOf", wrapType, wrapType, e);
            assert wrapType.isSubtype(to);
            return v.tnf.Cast(wrap, to);
        }
    }

    /** Convert boxed value to primitive value. */
    protected Node unboxingConversion(
            DesugarLocally v, Expr e, ClassType from, PrimitiveType to) {

        assert v.ts.isPrimitiveWrapper(from);
        String valueMethod = to.name() + "Value";
        return v.tnf.Call(e.position(), e, valueMethod, from, to);
    }

    /** Down cast. */
    protected Node narrowingReferenceConversion(
            DesugarLocally v, Expr e, ReferenceType from, ReferenceType to) {
        Position pos = e.position();
        LocalDecl val = v.tnf.TempSSA("castExpr", e);
        Throw throwExn = v.tnf.Throw(pos, v.ts.ClassCastException(), Collections.emptyList());
        Instanceof check = v.tnf.InstanceOf(v.tnf.Local(pos, val), to);
        If guard = v.tnf.If(v.tnf.Not(check), throwExn);
        Cast cast = v.tnf.Cast(v.tnf.Local(pos, val), to);
        Cast bitcast = context(cast, GUARDED_BITCAST);
        return v.tnf.ESeq(Arrays.asList(val, guard), bitcast);
    }

    /** Convert any expression to a string. */
    protected Node stringConversion(DesugarLocally v, Expr e, Type from) {
        Position pos = e.position();

        if (from.isNull()) {
            // Substitute "null".
            return v.tnf.StringLit(pos, "null");
        }
        else if (from.isPrimitive()) {
            // Call String.valueOf(...)
            return v.tnf.StaticCall(pos, "valueOf", v.ts.String(), v.ts.String(), e);
        }
        else if (e instanceof StringLit) {
            // Optimization.
            return e;
        }
        else if (e instanceof Binary
                && ((Binary) e).operator().equals(Binary.ADD)
                && e.type().typeEquals(v.ts.String())) {
            // Optimization.
            return e;
        }
        else {
            assert from.isReference();

            // Call toString(...) in the runtime library, which will have the right semantics
            // if e has a null value or if e.toString() has a null value.
            // This is needed even for String types, since they could be null.
            ClassType helperType = v.tnf.typeForName(Constants.RUNTIME_HELPER).toClass();
            return v.tnf.StaticCall(pos, "toString", helperType, v.ts.String(), e);
        }
    }

    /** Convert from one primitive to another. */
    protected LLVMValueRef primitiveConversion(
            LLVMTranslator v, LLVMValueRef e, PrimitiveType from, PrimitiveType to) {
        assert !(from.isByte() && to.isChar())
                : "This requires two conversions; should have ben desugared";
        LLVMTypeRef llT = v.utils.toLL(to);
        String name = "cast";

        if (from.isLongOrLess()) {
            // From integral type.
            if (to.isLongOrLess()) {
                // To integral type.
                if (v.utils.sizeOfType(to) > v.utils.sizeOfType(from)) {
                    // Of larger size.
                    return from.isChar()
                            ? LLVMBuildZExtOrBitCast(v.builder, e, llT, name)
                            : LLVMBuildSExtOrBitCast(v.builder, e, llT, name);
                } else {
                    // Of smaller size.
                    return LLVMBuildTruncOrBitCast(v.builder, e, llT, name);
                }
            } else {
                // To floating-point type.
                return from.isChar()
                        ? LLVMBuildUIToFP(v.builder, e, llT, name)
                        : LLVMBuildSIToFP(v.builder, e, llT, name);
            }
        } else {
            // From floating-point type.
            if (to.isLongOrLess()) {
                // To integral type.
                return to.isChar()
                        ? LLVMBuildFPToUI(v.builder, e, llT, name)
                        : LLVMBuildFPToSI(v.builder, e, llT, name);
            } else {
                // To floating-point type.
                return LLVMBuildFPCast(v.builder, e, llT, name);
            }
        }
    }
}
