package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.*;
import polyglot.ast.Node;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Arrays;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Binary.*;

public class PolyLLVMBinaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        Binary n = (Binary) node();

        // Desugar string concatenation into a method call.
        ClassType strT = v.ts.String();
        if (n.operator().equals(Binary.ADD) && n.type().typeEquals(strT)) {
            assert n.left().type().typeEquals(strT) && n.right().type().typeEquals(strT);
            return v.tnf.Call(n.position(), n.left(), "concat", strT, strT, n.right());
        }

        return super.desugar(v);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Binary n = (Binary) node();
        Type resType = n.type();
        Operator op = n.operator();

        if (op.equals(Binary.COND_AND) || op.equals(Binary.COND_OR)) {
            LLVMValueRef res = computeShortCircuitOp(v, resType);
            v.addTranslation(n, res);
            return n;
        }

        return super.overrideTranslateLLVM(parent, v);
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Binary n = (Binary) node();
        Type resType = n.type();
        LLVMValueRef left = v.getTranslation(n.left());
        LLVMValueRef right = v.getTranslation(n.right());
        Operator op = n.operator();
        Type elemType = n.left().type();

        boolean isShift = Arrays.asList(SHL, SHR, USHR).contains(op);
        boolean isBitwiseOp = Arrays.asList(BIT_AND, BIT_OR, BIT_XOR).contains(op);

        LLVMValueRef res;
        if (isShift) {
            // Shift operation. See JLS SE 7, section 15.19.
            // First ensure that the shift operand has the same type as the shifted operand.
            right = LLVMBuildIntCast(v.builder, right, LLVMTypeOf(left), "shift.cast");

            // Then mask the shift operand to keep JLS-defined behavior.
            int numBits = v.utils.numBitsOfIntegralType(elemType.toPrimitive());
            LLVMValueRef mask = LLVMConstInt(LLVMTypeOf(right), numBits - 1, /*sign-extend*/ 0);
            right = LLVMBuildAnd(v.builder, right, mask, "shift.mask");

            res = LLVMBuildBinOp(v.builder, llvmIntBinopCode(op, elemType), left, right, "shift");
        }
        else if (resType.isLongOrLess() || (resType.isBoolean() && isBitwiseOp)) {
            // Integer binop or boolean logical operator.
            res = LLVMBuildBinOp(v.builder, llvmIntBinopCode(op, elemType), left, right, "ibinop");
        }
        else if (resType.isFloat() || resType.isDouble()) {
            // Floating point binop.
            res = LLVMBuildBinOp(v.builder, llvmFloatBinopCode(op), left, right, "fbinop");
        }
        else if (resType.isBoolean() && (elemType.isFloat() || elemType.isDouble())) {
            // Floating point comparison.
            res = LLVMBuildFCmp(v.builder, llvmFCmpBinopCode(op), left, right, "fcmp");
        }
        else if (resType.isBoolean()
                && (elemType.isLongOrLess() || elemType.isBoolean() || elemType.isReference())) {
            // Integer or boolean or reference comparison.
            res = LLVMBuildICmp(v.builder, llvmICmpBinopCode(op, elemType), left, right, "icmp");
        }
        else {
            throw new InternalCompilerError("Invalid binary operation result type");
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public void translateLLVMConditional(LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        Binary n = (Binary) node();
        Operator op = n.operator();
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

    protected LLVMValueRef computeShortCircuitOp(LLVMTranslator v, Type resType) {
        LLVMValueRef binopRes = v.utils.buildAlloca("binop.res", v.utils.toLL(resType));
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

    protected static boolean isUnsigned(Type t) {
        return t.isChar() || t.isBoolean();
    }

    protected static int llvmIntBinopCode(Operator op, Type type) {
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
        else throw new InternalCompilerError("Invalid integer operation: " + op);
    }

    protected static int llvmFloatBinopCode(Operator op) {
        if      (op == ADD) return LLVMFAdd;
        else if (op == SUB) return LLVMFSub;
        else if (op == MUL) return LLVMFMul;
        else if (op == DIV) return LLVMFDiv;
        else throw new InternalCompilerError("Invalid floating point operation: " + op);
    }

    protected static int llvmICmpBinopCode(Operator op, Type t) {
        if      (op == LT) return isUnsigned(t) ? LLVMIntULT : LLVMIntSLT;
        else if (op == LE) return isUnsigned(t) ? LLVMIntULE : LLVMIntSLE;
        else if (op == EQ) return LLVMIntEQ;
        else if (op == NE) return LLVMIntNE;
        else if (op == GE) return isUnsigned(t) ? LLVMIntUGE : LLVMIntSGE;
        else if (op == GT) return isUnsigned(t) ? LLVMIntUGT : LLVMIntSGT;
        else throw new InternalCompilerError("This operation is not a comparison: " + op);
    }

    protected static int llvmFCmpBinopCode(Operator op) {
        // Java floating point uses ordered comparisons (i.e., comparisons with NaN return false).
        if      (op == LT) return LLVMRealOLT;
        else if (op == LE) return LLVMRealOLE;
        else if (op == EQ) return LLVMRealOEQ;
        else if (op == NE) return LLVMRealONE;
        else if (op == GE) return LLVMRealOGE;
        else if (op == GT) return LLVMRealOGT;
        else throw new InternalCompilerError("This operation is not a comparison: " + op);
    }
}
