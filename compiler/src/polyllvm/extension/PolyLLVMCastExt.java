package polyllvm.extension;

import polyglot.ast.Cast;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCastExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        // TODO: Double-check semantics with http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html

        Cast n = (Cast) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Type exprType = n.expr().type();

        Type castType = n.castType().type();
        LLVMTypeRef castTypeRef = v.utils.typeRef(castType);

        LLVMValueRef exprTranslation = v.getTranslation(n.expr());

        // The cast is an identity cast.
        if (exprType.typeEquals(castType)) {
            v.addTranslation(n, exprTranslation);
            return super.translatePseudoLLVM(v);
        }

        v.debugInfo.emitLocation(n);

        if (exprType.isPrimitive() && castType.isPrimitive()) {
            if (exprType.isLongOrLess() && castType.isLongOrLess()) {
                // Integral primitives.
                if (exprType.isChar() && !castType.isByte()) {
                    // A widening conversion of a char to an integral type T zero-extends
                    // the representation of the char value to fill the wider format.
                    v.addTranslation(n, LLVMBuildZExt(v.builder, exprTranslation, castTypeRef, "cast"));
                }
                else if (exprType.isImplicitCastValid(castType)) {
                    // Sign-extending widening cast.
                    v.addTranslation(n, LLVMBuildSExt(v.builder, exprTranslation, castTypeRef, "cast"));
                }
                else if (exprType.isByte() && castType.isChar()) {
                    // Java language spec: first, the byte is converted to an int via widening
                    // primitive conversion (5.1.2), and then the resulting int is converted to a
                    // char by narrowing primitive conversion (5.1.3).
                    v.addTranslation(n, LLVMBuildSExt(v.builder, exprTranslation, castTypeRef, "cast"));
                }
                else {
                    // Truncation.
                    v.addTranslation(n, LLVMBuildTrunc(v.builder, exprTranslation, castTypeRef, "cast"));

                }
            } else if (exprType.isLongOrLess()) {
                // Integral primitive to floating point primitive.
                // TODO: Should sitofp know about float vs. double?
                v.addTranslation(n, LLVMBuildSIToFP(v.builder, exprTranslation, castTypeRef, "cast"));
            } else if (exprType.isFloat() && castType.isDouble()) {
                // Float to double.
                v.addTranslation(n, LLVMBuildFPExt(v.builder, exprTranslation, castTypeRef, "cast"));

            } else {
                // TODO: Handle casts from double to float?
                throw new InternalCompilerError("Unhandled cast: " + n);
            }
        }
        else if (!castType.isPrimitive() && !exprType.isPrimitive()) {
            if (exprType.isImplicitCastValid(castType)) {
                // This is an implicit reference cast.
                v.addTranslation(n, LLVMBuildBitCast(v.builder, exprTranslation, castTypeRef, "cast"));
            } else {
                // TODO: Need runtime check to catch invalid down-casts.
                v.addTranslation(n, LLVMBuildBitCast(v.builder, exprTranslation, castTypeRef, "cast"));
            }
        }
        else {
            throw new InternalCompilerError("Unhandled cast: " + n);
        }
        return super.translatePseudoLLVM(v);
    }
}
