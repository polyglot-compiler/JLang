package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.*;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Binary.*;

public class PolyLLVMBinaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Binary n = (Binary) node();
        Type resType = n.type();
        Operator op = n.operator();

        if (op.equals(Binary.COND_AND) || op.equals(Binary.COND_OR)) {
            LLVMValueRef res = computeShortCircuitOp(v, resType);
            v.addTranslation(n, res);
            return n;
        }

        return super.overrideTranslateLLVM(v);
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Binary n = (Binary) node();
        Type resType = n.type();
        LLVMValueRef left = v.getTranslation(n.left());
        LLVMValueRef right = v.getTranslation(n.right());
        Operator op = n.operator();
        Type elemType = n.left().type();

        v.debugInfo.emitLocation(n);
        LLVMValueRef res = computeBinop(v.builder, op, left, right, resType, elemType);
        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public void translateLLVMConditional(LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        Binary n = (Binary) node();
        Operator op = n.operator();
        v.debugInfo.emitLocation(n);
        if (op.equals(Binary.COND_AND)) {
            LLVMBasicBlockRef l1 = v.utils.buildBlock("l1");
            lang().translateLLVMConditional(n.left(), v, l1, falseBlock);
            LLVMPositionBuilderAtEnd(v.builder, l1);
            lang().translateLLVMConditional(n.right(), v, trueBlock, falseBlock);
        }
        else if (op.equals(Binary.COND_OR)) {
            LLVMBasicBlockRef l1 = v.utils.buildBlock("l1");
            lang().translateLLVMConditional(n.left(), v, trueBlock, l1);
            LLVMPositionBuilderAtEnd(v.builder, l1);
            lang().translateLLVMConditional(n.right(), v, trueBlock, falseBlock);
        }
        else {
            super.translateLLVMConditional(v, trueBlock, falseBlock);
        }
    }

    static LLVMValueRef computeBinop(LLVMBuilderRef builder,
                                     Operator op, LLVMValueRef left, LLVMValueRef right,
                                     Type resType, Type elemType) {
        if (resType.isLongOrLess()) {
            // Integer binop.
            return LLVMBuildBinOp(builder, llvmIntBinopCode(op, elemType), left, right, "ibinop");
        } else if (resType.isFloat() || resType.isDouble()) {
            // Floating point binop.
            return LLVMBuildBinOp(builder, llvmFloatBinopCode(op), left, right, "fbinop");
        } else if (resType.isBoolean() && (elemType.isFloat() || elemType.isDouble())) {
            // Floating point comparison.
            return LLVMBuildFCmp(builder, llvmFCmpBinopCode(op), left, right, "fcmp");
        } else if (resType.isBoolean() && (elemType.isLongOrLess() || elemType.isBoolean() || elemType.isReference())) {
            // Integer comparison.
            return LLVMBuildICmp(builder, llvmICmpBinopCode(op, elemType), left, right, "icmp");
        } else {
            throw new InternalCompilerError("Invalid binary operation result type");
        }
    }

    private LLVMValueRef computeShortCircuitOp(LLVMTranslator v, Type resType) {
        LLVMValueRef binopRes = PolyLLVMLocalDeclExt.createLocal(v, "binop_res", v.utils.toLL(resType));
        LLVMBasicBlockRef trueBranch = v.utils.buildBlock("true_branch");
        LLVMBasicBlockRef falseBranch = v.utils.buildBlock("false_branch");
        LLVMBasicBlockRef continueBranch = v.utils.buildBlock("continue");

        translateLLVMConditional(v, trueBranch, falseBranch);

        LLVMValueRef one = LLVMConstInt(v.utils.toLL(resType), 1, /*sign-extend*/ 0);
        LLVMPositionBuilderAtEnd(v.builder, trueBranch);
        LLVMBuildStore(v.builder, one, binopRes);
        LLVMBuildBr(v.builder, continueBranch);

        LLVMValueRef zero = LLVMConstInt(v.utils.toLL(resType), 0, /*sign-extend*/ 0);
        LLVMPositionBuilderAtEnd(v.builder, falseBranch);
        LLVMBuildStore(v.builder, zero, binopRes);
        LLVMBuildBr(v.builder, continueBranch);

        LLVMPositionBuilderAtEnd(v.builder, continueBranch);

        return LLVMBuildLoad(v.builder, binopRes, "binop");
    }

    private static boolean isUnsigned(Type t) {
        return t.isChar() || t.isBoolean();
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
}
