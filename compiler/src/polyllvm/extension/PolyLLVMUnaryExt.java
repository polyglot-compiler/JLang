package polyllvm.extension;

import polyglot.ast.*;
import polyglot.ast.Unary.*;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Arrays;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Unary.*;

public class PolyLLVMUnaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Unary n = (Unary) node();
        if (Arrays.asList(PRE_INC, PRE_DEC, POST_INC, POST_DEC).contains(n.operator())) {
            // JL5UnaryExt would return an unboxing conversion here, but
            // it's not clear why. We fix that here, since we want the
            // boxed value when we desugar.
            return child.type();
        }
        return super.childExpectedType(child, av);
    }

    @Override
    public Node desugar(DesugarLocally v) {
        Unary n = (Unary) node();

        // Desugar increment/decrement into ESeq nodes with a Binary child.
        if (Arrays.asList(PRE_INC, PRE_DEC, POST_INC, POST_DEC).contains(n.operator()))
            return desugarIncOrDec(n, v);

        return super.desugar(v);
    }

    protected Node desugarIncOrDec(Unary n, DesugarLocally v) {
        Position pos = n.position();
        Operator op = n.operator();
        boolean pre = op.equals(PRE_INC) || op.equals(PRE_DEC);
        boolean inc = op.equals(PRE_INC) || op.equals(POST_INC);

        // Get the address and value of the expression.
        LocalDecl ptrFlat = v.tnf.TempSSA("lvalue", v.tnf.AddressOf(n.expr()));
        Local ptr = v.tnf.Local(pos, ptrFlat);
        LocalDecl ptrLoadedFlat = v.tnf.TempSSA("load", v.tnf.Load(copy(ptr)));
        Local ptrLoaded = v.tnf.Local(pos, ptrLoadedFlat);

        // Compute the binop.
        Binary.Operator binop = inc ? Binary.ADD : Binary.SUB;
        IntLit one = (IntLit) v.nf.IntLit(pos, IntLit.INT, 1).type(v.ts.Int());
        Binary bin = v.nf.Binary(pos, copy(ptrLoaded), binop, one);
        try {
            // Numeric promotion. (Should result in a numeric type.)
            bin = (Binary) bin.type(v.ts.promote(bin.left().type(), bin.right().type()));
            assert bin.type().isNumeric();
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }

        // Store the result and return the correct value.
        LocalDecl resFlat = v.tnf.TempSSA("res", v.tnf.Cast(bin, n.expr().type()));
        Local res = v.tnf.Local(pos, resFlat);
        Stmt update = v.tnf.EvalAssign(copy(ptr), copy(res));
        Expr val = pre ? copy(res) : copy(ptrLoaded);

        return v.tnf.ESeq(Arrays.asList(ptrFlat, ptrLoadedFlat, resFlat, update), val);
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Unary n = (Unary) node();
        Operator op = n.operator();
        LLVMValueRef exprRef = v.getTranslation(n.expr());
        LLVMTypeRef typeRef = v.utils.toLL(n.expr().type());

        LLVMValueRef translation;
        if (op.equals(BIT_NOT)) {
            LLVMValueRef negOne = LLVMConstInt(typeRef, -1, /*sign-extend*/ 0);
            translation = LLVMBuildXor(v.builder, exprRef, negOne, "bit.not");
        }
        else if (op.equals(NEG)) {
            translation = n.type().isLongOrLess()
                    ? LLVMBuildNeg (v.builder, exprRef, "neg")
                    : LLVMBuildFNeg(v.builder, exprRef, "neg");
        }
        else if (op.equals(POS)) {
            translation = exprRef;
        }
        else if (op.equals(NOT)) {
            assert n.type().typeEquals(v.ts.Boolean());
            translation = LLVMBuildNot(v.builder, exprRef, "not");
        }
        else {
            throw new InternalCompilerError("Unknown unary operation " + op);
        }

        v.addTranslation(n, translation);
        return super.leaveTranslateLLVM(v);
    }

}
