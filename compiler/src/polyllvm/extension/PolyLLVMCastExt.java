package polyllvm.extension;

import polyglot.ast.Cast;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCastExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        // TODO: Double-check semantics with http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html

        Cast n = (Cast) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Type exprType = n.expr().type();
        LLVMTypeNode exprTypeNode = LLVMUtils.polyLLVMTypeNode(nf, exprType);

        Type castType = n.castType().type();
        LLVMTypeNode castTypeNode = LLVMUtils.polyLLVMTypeNode(nf, castType);

        LLVMOperand exprTranslation = (LLVMOperand) v.getTranslation(n.expr());

        // The cast is an identity cast.
        if (exprType.typeEquals(castType)) {
            v.addTranslation(n, v.getTranslation(n.expr()));
            return super.translatePseudoLLVM(v);
        }

        Instruction instructionType;
        if (exprType.isPrimitive() && castType.isPrimitive()) {
            if (exprType.isLongOrLess() && castType.isLongOrLess()) {
                // Integral primitives.
                if (exprType.isChar() && !castType.isByte()) {
                    // A widening conversion of a char to an integral type T zero-extends
                    // the representation of the char value to fill the wider format.
                    instructionType = LLVMConversion.ZEXT;
                }
                else if (exprType.isImplicitCastValid(castType)) {
                    // Sign-extending widening cast.
                    instructionType = LLVMConversion.SEXT;
                }
                else if (exprType.isByte() && castType.isChar()) {
                    // Java language spec: first, the byte is converted to an int via widening
                    // primitive conversion (5.1.2), and then the resulting int is converted to a
                    // char by narrowing primitive conversion (5.1.3).
                    instructionType = LLVMConversion.SEXT;
                }
                else {
                    // Truncation.
                    instructionType = LLVMConversion.TRUNC;
                }
            } else if (exprType.isLongOrLess()) {
                // Integral primitive to floating point primitive.
                // TODO: Should sitofp know about float vs. double?
                instructionType = LLVMConversion.SITOFP;
            } else if (exprType.isFloat() && castType.isDouble()) {
                // Float to double.
                instructionType = LLVMConversion.FPEXT;
            } else {
                // TODO: Handle casts from double to float?
                throw new InternalCompilerError("Unhandled cast: " + n);
            }
        }
        else if (!castType.isPrimitive() && !exprType.isPrimitive()) {
            if (exprType.isImplicitCastValid(castType)) {
                // This is an implicit reference cast.
                instructionType = LLVMConversion.BITCAST;
            } else {
                // TODO: Need runtime check to catch invalid down-casts.
                instructionType = LLVMConversion.BITCAST;
            }
        }
        else {
            throw new InternalCompilerError("Unhandled cast: " + n);
        }

        LLVMInstruction conv = nf.LLVMConversion(
                instructionType,
                exprTypeNode,
                exprTranslation,
                castTypeNode);
        LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, castTypeNode);
        conv = conv.result(result);
        v.addTranslation(n, nf.LLVMESeq(conv, result));

        return super.translatePseudoLLVM(v);
    }
}
