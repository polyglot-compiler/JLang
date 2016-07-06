package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.ast.Unary.Operator;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
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
        }
        else if (Unary.NEG == op) {
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
        }
        else if (Unary.NOT == op) {
            //Easy case: expr will be boolean
            // Use expr XOR 1
        }
        else {

        }
        return super.translatePseudoLLVM(v);
    }

}
