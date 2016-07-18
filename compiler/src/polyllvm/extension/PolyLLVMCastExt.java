package polyllvm.extension;

import polyglot.ast.Cast;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCastExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Cast n = (Cast) node();

        PolyLLVMNodeFactory nf = v.nodeFactory();

        Type exprType = n.expr().type();
        LLVMTypeNode exprTypeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, exprType);
        Type castType = n.castType().type();
        LLVMTypeNode castTypeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, castType);

        LLVMOperand exprTranslation = (LLVMOperand) v.getTranslation(n.expr());

        // The cast is an identity cast
        if (exprType.typeEquals(castType)) {
            v.addTranslation(n, v.getTranslation(n.expr()));
            return super.translatePseudoLLVM(v);
        }

        //This is a primitive widening conversion
        if (castType.isPrimitive() && exprType.isPrimitive()
                && exprType.isImplicitCastValid(castType)) {
            Instruction instructionType;

            //A widening conversion of a char to an integral type T zero-extends
            //the representation of the char value to fill the wider format.
            if (exprType.isChar() && castType.isLongOrLess()) {
                //Zero extend to n.castType().type()
                //LLVM : zext
                instructionType = LLVMConversion.ZEXT;
            }
            //A widening conversion of a signed integer value to an integral
            //type T simply sign-extends the two's-complement representation
            //of the integer value to fill the wider format.
            else if (exprType.isLongOrLess() && castType.isLongOrLess()) {
                //Sign extend Expr to n.castType().type()
                //LLVM : sext
                instructionType = LLVMConversion.SEXT;
            }
            else if (exprType.isLongOrLess()
                    && (castType.isFloat() || castType.isDouble())) {
                // LLVM : sitofp
                instructionType = LLVMConversion.SITOFP;
            }
            else if (exprType.isFloat() && castType.isDouble()) {
                // LLVM : fpext
                instructionType = LLVMConversion.FPEXT;
            }
            else {
                throw new InternalCompilerError("Unhandled primitive widening cast: "
                        + n);
            }
            LLVMInstruction conv =
                    nf.LLVMConversion(Position.compilerGenerated(),
                                      instructionType,
                                      exprTypeNode,
                                      exprTranslation,
                                      castTypeNode);
            LLVMVariable result =
                    PolyLLVMFreshGen.freshLocalVar(nf, castTypeNode);
            conv = conv.result(result);
            v.addTranslation(n,
                             nf.LLVMESeq(conv,
                                         result));

        }
        return super.translatePseudoLLVM(v);
    }
}
