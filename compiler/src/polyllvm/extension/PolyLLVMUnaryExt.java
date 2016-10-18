package polyllvm.extension;

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
        System.out.println("Translating Unary : " + node());
        Unary n = (Unary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Operator op = n.operator();
        Expr expr = n.expr();
        LLVMOperand exprTranslation = (LLVMOperand) v.getTranslation(expr);
        if (Unary.BIT_NOT == op) {
            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, exprTranslation.typeNode());
            LLVMInstruction xor =
                    nf.LLVMBitwiseBinaryInstruction(LLVMBitwiseBinaryInstruction.XOR,
                            result, (LLVMIntType) exprTranslation.typeNode(),
                            exprTranslation, nf.LLVMIntLiteral(exprTranslation.typeNode(), -1));
            LLVMExpr eseq = nf.LLVMESeq(xor, result);
            v.addTranslation(n, eseq);
        }
        else if (Unary.NEG == op) {
            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, exprTranslation.typeNode());
            LLVMInstruction sub =
                    nf.LLVMSub(result, (LLVMIntType) exprTranslation.typeNode(),
                            nf.LLVMIntLiteral(exprTranslation.typeNode(), 0), exprTranslation);
            LLVMExpr eseq = nf.LLVMESeq(sub, result);
            v.addTranslation(n, eseq);
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
            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, exprTranslation.typeNode());
            LLVMInstruction add =
                    nf.LLVMAdd(result, (LLVMIntType) exprTranslation.typeNode(),
                            nf.LLVMIntLiteral(exprTranslation.typeNode(), 0), exprTranslation);
            LLVMExpr eseq = nf.LLVMESeq(add, result);
            v.addTranslation(n, eseq);
        }
        else if (Unary.NOT == op) {
            //Easy case: expr will be boolean
            // Use expr XOR 1
            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, exprTranslation.typeNode());
            LLVMInstruction xor =
                    nf.LLVMBitwiseBinaryInstruction(LLVMBitwiseBinaryInstruction.XOR,
                            result, (LLVMIntType) exprTranslation.typeNode(),
                            exprTranslation, nf.LLVMIntLiteral(exprTranslation.typeNode(), 1));
            LLVMExpr eseq = nf.LLVMESeq(xor, result);
            v.addTranslation(n, eseq);
        }
        else {

        }
        return super.translatePseudoLLVM(v);
    }

}
