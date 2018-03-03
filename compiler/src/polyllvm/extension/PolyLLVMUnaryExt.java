package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.ast.Unary.*;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Arrays;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Unary.*;

public class PolyLLVMUnaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        // Override for increment operations on lvalues (++, --, etc.).
        Unary n = (Unary) node();
        Operator op = n.operator();
        if (!Arrays.asList(PRE_INC, PRE_DEC, POST_INC, POST_DEC).contains(op))
            return super.overrideTranslateLLVM(parent, v); // Translate other operations normally.

        boolean pre = op.equals(PRE_INC) || op.equals(PRE_DEC);
        boolean inc = op.equals(PRE_INC) || op.equals(POST_INC);
        boolean integral = n.expr().type().isLongOrLess();
        Binary.Operator binop = inc ? Binary.ADD : Binary.SUB;
        Type type = n.expr().type();
        LLVMTypeRef typeRef = v.utils.toLL(type);

        // Get lvalue.
        LLVMValueRef exprPtr = lang().translateAsLValue(n.expr(), v);

        // Increment and store.
        LLVMValueRef one = integral
                ? LLVMConstInt(typeRef, 1, /*sign-extend*/ 0)
                : LLVMConstReal(typeRef, 1.);
        LLVMValueRef exprRef = LLVMBuildLoad(v.builder, exprPtr, "load");
        LLVMValueRef newVal
                = PolyLLVMBinaryExt.computeBinop(v.builder, binop, exprRef, one, type, type);
        LLVMBuildStore(v.builder, newVal, exprPtr);

        // Choose old or new value.
        LLVMValueRef translation = pre ? newVal : exprRef;
        v.addTranslation(n, translation);

        return n;
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Unary n = (Unary) node();
        Type t = n.type();
        Operator op = n.operator();
        Expr expr = n.expr();
        LLVMValueRef exprRef = v.getTranslation(expr);
        LLVMTypeRef typeRef = v.utils.toLL(expr.type());

        LLVMValueRef translation;
        if (op.equals(BIT_NOT)) {
            LLVMValueRef negOne = LLVMConstInt(typeRef, -1, /* sign-extend */ 0);
            translation = LLVMBuildXor(v.builder, exprRef, negOne, "bit_not");
        }
        else if (op.equals(NEG)) {
            translation = t.isLongOrLess()
                    ? LLVMBuildNeg (v.builder, exprRef, "neg")
                    : LLVMBuildFNeg(v.builder, exprRef, "neg");
        }
        else if (op.equals(POS)) {
            translation = exprRef;
        }
        else if (op.equals(NOT)) {
            assert t.typeEquals(v.typeSystem().Boolean());
            translation = LLVMBuildNot(v.builder, exprRef, "not");
        }
        else {
            throw new InternalCompilerError("Invalid unary operation");
        }

        v.addTranslation(n, translation);
        return super.leaveTranslateLLVM(v);
    }

}
