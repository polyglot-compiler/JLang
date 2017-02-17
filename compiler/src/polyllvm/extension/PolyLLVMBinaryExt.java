package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.*;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Binary.*;

public class PolyLLVMBinaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private static boolean isUnsigned(Type t) {
        return t.isChar();
    }

    private static int llvmIntBinopCode(Operator op, Type type) {
        if      (op == ADD)     return LLVMAdd;
        else if (op == SUB)     return LLVMSub;
        else if (op == MUL)     return LLVMMul;
        else if (op == DIV)     return isUnsigned(type) ? LLVMUDiv : LLVMSDiv;
        else if (op == MOD)     return isUnsigned(type) ? LLVMURem : LLVMSRem;
        else if (op == BIT_OR)  return LLVMOr;
        else if (op == BIT_AND) return LLVMAnd;
        else if (op == BIT_XOR) return LLVMXor;
        else if (op == SHL)     return LLVMShl;
        else if (op == USHR)    return LLVMLShr;
        else if (op == SHR)     return isUnsigned(type) ? LLVMLShr : LLVMAShr;
        else throw new InternalCompilerError("Invalid integer operation");
    }

    private static int llvmFloatBinopCode(Operator op) {
        if      (op == ADD) return LLVMFAdd;
        else if (op == SUB) return LLVMFSub;
        else if (op == MUL) return LLVMFMul;
        else if (op == DIV) return LLVMFDiv;
        else throw new InternalCompilerError("Invalid floating point operation");
    }

    private static int llvmICmpBinopCode(Operator op, Type t) {
        if      (op == LT) return isUnsigned(t) ? LLVMIntULT : LLVMIntSLT;
        else if (op == LE) return isUnsigned(t) ? LLVMIntULE : LLVMIntSLE;
        else if (op == EQ) return LLVMIntEQ;
        else if (op == NE) return LLVMIntNE;
        else if (op == GE) return isUnsigned(t) ? LLVMIntUGE : LLVMIntSGE;
        else if (op == GT) return isUnsigned(t) ? LLVMIntUGT : LLVMIntSGT;
        else throw new InternalCompilerError("This operation is not a comparison");
    }

    private static int llvmFCmpBinopCode(Operator op) {
        // Java floating point uses ordered comparisons (i.e., comparisons with NaN return false).
        if      (op == LT) return LLVMRealOLT;
        else if (op == LE) return LLVMRealOLE;
        else if (op == EQ) return LLVMRealOEQ;
        else if (op == NE) return LLVMRealONE;
        else if (op == GE) return LLVMRealOGE;
        else if (op == GT) return LLVMRealOGT;
        else throw new InternalCompilerError("This operation is not a comparison");
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Binary n = (Binary) node();
        Type resType = n.type();
        LLVMValueRef left = v.getTranslation(n.left());
        LLVMValueRef right = v.getTranslation(n.right());
        Operator op = n.operator();

        assert n.left().type().typeEquals(n.right().type());
        Type elemType = n.left().type();

        LLVMValueRef res;
        if (resType.isLongOrLess()) {
            // Integer binop.
            res = LLVMBuildBinOp(v.builder, llvmIntBinopCode(op, elemType), left, right, "binop");
        } else if (resType.isFloat() || resType.isDouble()) {
            // Floating point binop.
            res = LLVMBuildBinOp(v.builder, llvmFloatBinopCode(op), left, right, "binop");
        } else if (resType.isBoolean() && elemType.isLongOrLess()) {
            // Integer comparison.
            res = LLVMBuildICmp(v.builder, llvmICmpBinopCode(op, elemType), left, right, "cmp");
        } else if (resType.isBoolean() && (elemType.isFloat() || elemType.isDouble())) {
            // Floating point comparison.
            res = LLVMBuildFCmp(v.builder, llvmFCmpBinopCode(op), left, right, "cmp");
        } else {
            throw new InternalCompilerError("Invalid binary operation result type");
        }

        v.addTranslation(n, res);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public void translateLLVMConditional(PseudoLLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        Binary n = (Binary) node();
        Operator op = n.operator();
        if (op.equals(Binary.COND_AND)) {
            LLVMBasicBlockRef l1 = LLVMAppendBasicBlock(v.currFn(), "l1");
            lang().translateLLVMConditional(n.left(), v, l1, falseBlock);
            LLVMPositionBuilderAtEnd(v.builder, l1);
            lang().translateLLVMConditional(n.right(), v, trueBlock, falseBlock);
        }
        else if (op.equals(Binary.COND_OR)) {
            LLVMBasicBlockRef l1 = LLVMAppendBasicBlock(v.currFn(), "l1");
            lang().translateLLVMConditional(n.left(), v, trueBlock, l1);
            LLVMPositionBuilderAtEnd(v.builder, l1);
            lang().translateLLVMConditional(n.right(), v, trueBlock, falseBlock);
        }
        else {
            super.translateLLVMConditional(v, trueBlock, falseBlock);
        }
    }
}
