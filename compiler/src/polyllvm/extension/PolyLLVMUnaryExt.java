package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.ast.Unary.Operator;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMExpr;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBitwiseBinaryInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMUnaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Unary n = (Unary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Operator op = n.operator();
        Expr expr = n.expr();

        if (Unary.NEG == op && expr.type().isIntOrLess()) {
            expr = nf.Cast(Position.compilerGenerated(),
                           nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                v.typeSystem().Int()),
                           expr)
                     .type(v.typeSystem().Int());

        }
        else if (Unary.POS == op && expr.type().isIntOrLess()) {
            return nf.Cast(Position.compilerGenerated(),
                           nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                v.typeSystem().Int()),
                           expr)
                     .type(v.typeSystem().Int());
        }

        return n.expr(expr);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Unary n = (Unary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Operator op = n.operator();
        Expr expr = n.expr();
        LLVMValueRef exprTranslation = v.getTranslation(expr);
        if (Unary.BIT_NOT == op) {
            LLVMValueRef bitnot = LLVMBuildXor(v.builder, exprTranslation,
                    LLVMConstInt(LLVMUtils.typeRef(expr.type(), v), -1, /* sign-extend */ 0), "bit_not");
            v.addTranslation(n, bitnot);
        }
        else if (Unary.NEG == op) {
            LLVMValueRef neg = LLVMBuildSub(v.builder,
                    LLVMConstInt(LLVMUtils.typeRef(expr.type(), v), 0, /* sign-extend */ 0),
                    exprTranslation, "neg");
            v.addTranslation(n, neg);
        }
        else if (Unary.POST_INC == op) {
        }
        else if (Unary.POST_DEC == op) {
        }
        else if (Unary.PRE_INC == op) {
        }
        else if (Unary.PRE_DEC == op) {
        }
        else if (Unary.POS == op) {
            LLVMValueRef neg = LLVMBuildAdd(v.builder, exprTranslation,
                    LLVMConstInt(LLVMUtils.typeRef(expr.type(), v), 0, /* sign-extend */ 0), "pos");
            v.addTranslation(n, neg);
        }
        else if (Unary.NOT == op) {
            //Easy case: expr will be boolean
            // Use expr XOR 1
            LLVMValueRef neg = LLVMBuildXor(v.builder, exprTranslation,
                    LLVMConstInt(LLVMUtils.typeRef(expr.type(), v), 1, /* sign-extend */ 0), "pos");
            v.addTranslation(n, neg);
        }
        else {

        }
        return super.translatePseudoLLVM(v);
    }

}
