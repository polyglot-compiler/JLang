package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.LLVMTranslator;

import java.util.ArrayList;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCastExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        // TODO: Double-check semantics with http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html

        Cast n = (Cast) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Type exprType = n.expr().type();

        Type castType = n.castType().type();
        LLVMTypeRef castTypeRef = v.utils.toLL(castType);

        LLVMValueRef exprRef = v.getTranslation(n.expr());

        // The cast is an identity cast.
        if (exprType.typeEquals(castType)) {
            if (!LLVMTypeOf(exprRef).equals(castTypeRef)){
                v.addTranslation(n, LLVMBuildBitCast(v.builder, exprRef, castTypeRef, "generic_cast"));
            } else {
                v.addTranslation(n, exprRef);
            }
            return super.leaveTranslateLLVM(v);
        }

        if (exprType.isPrimitive() && castType.isPrimitive()) {
            if (exprType.isLongOrLess() && castType.isLongOrLess()) {
                // Integral primitives.
                if (exprType.isChar() && !castType.isByte()) {
                    // A widening conversion of a char to an integral type T zero-extends
                    // the representation of the char value to fill the wider format.
                    v.addTranslation(n, LLVMBuildZExt(v.builder, exprRef, castTypeRef, "cast"));
                }
                else if (exprType.isImplicitCastValid(castType)) {
                    // Sign-extending widening cast.
                    v.addTranslation(n, LLVMBuildSExt(v.builder, exprRef, castTypeRef, "cast"));
                }
                else if (exprType.isByte() && castType.isChar()) {
                    // Java language spec: first, the byte is converted to an int via widening
                    // primitive conversion (5.1.2), and then the resulting int is converted to a
                    // char by narrowing primitive conversion (5.1.3).
                    v.addTranslation(n, LLVMBuildSExt(v.builder, exprRef, castTypeRef, "cast"));
                }
                else {
                    // Truncation.
                    v.addTranslation(n, LLVMBuildTrunc(v.builder, exprRef, castTypeRef, "cast"));

                }
            } else if (exprType.isLongOrLess()) {
                // Integral primitive to floating point primitive.
                // TODO: Should sitofp know about float vs. double?
                v.addTranslation(n, LLVMBuildSIToFP(v.builder, exprRef, castTypeRef, "cast"));
            } else if (exprType.isFloat() && castType.isDouble()) {
                // Float to double.
                v.addTranslation(n, LLVMBuildFPExt(v.builder, exprRef, castTypeRef, "cast"));

            } else {
                // TODO: Handle casts from double to float?
                throw new InternalCompilerError("Unhandled cast: " + n);
            }
        }
        else if (!castType.isPrimitive() && !exprType.isPrimitive()) {
            if (exprType.isImplicitCastValid(castType)) {
                // This is an implicit reference cast.
                v.addTranslation(n, LLVMBuildBitCast(v.builder, exprRef, castTypeRef, "cast"));
            } else {
                Position pos = n.position();
                TypeSystem ts = v.typeSystem();

                LLVMValueRef instanceOf
                        = PolyLLVMInstanceofExt.buildInstanceOf(v, exprRef, castType.toReference());
                LLVMValueRef notInstanceOf = LLVMBuildNot(v.builder, instanceOf, "not_instance_of");

                PolyLLVMIfExt.buildIf(v, notInstanceOf, () -> {
                    CanonicalTypeNode exceptionType = nf.CanonicalTypeNode(pos, ts.ClassCastException());
                    ConstructorInstance constructor;
                    try {
                        constructor = ts.findConstructor(
                                ts.ClassCastException(),
                                new ArrayList<>(),
                                v.getCurrentClass().type(),
                                /*fromClient*/ true
                        );
                    } catch (SemanticException e){
                        throw new InternalCompilerError(e);
                    }
                    Expr classCastException = nf.New(pos, exceptionType, new ArrayList<>())
                            .constructorInstance(constructor)
                            .type(ts.ClassCastException());
                    Throw throwClassCast = nf.Throw(pos, classCastException);
                    throwClassCast.visit(v);
                });

                LLVMValueRef res = LLVMBuildBitCast(v.builder, exprRef, castTypeRef, "cast");
                v.addTranslation(n, res);
            }
        }
        else {
            throw new InternalCompilerError("Unhandled cast: " + n);
        }
        return super.leaveTranslateLLVM(v);
    }
}
