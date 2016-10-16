package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Return n = (Return) node();
        NodeFactory nf = v.nodeFactory();
        System.out.println("Is it breaking?    " + n);
        System.out.println("Current method?     " + v.getCurrentMethod());
        if (v.getCurrentMethod() instanceof MethodDecl) {
            TypeNode returnType =
                    ((MethodDecl) v.getCurrentMethod()).returnType();
            if (!returnType.type().equals(n.expr().type())) {
                Expr cast = nf
                              .Cast(Position.compilerGenerated(),
                                    returnType,
                                    n.expr())
                              .type(returnType.type());
                return n.expr(cast);
            }

        }
        else {
            System.out.println("addPrimitiveWideningCasts for return, current method is : "
                    + v.getCurrentMethod());
        }
        return super.addPrimitiveWideningCasts(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Return n = (Return) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Expr expr = n.expr();
        if (expr == null) {
            v.addTranslation(n, nf.LLVMRet());
        }
        else {
            LLVMOperand o = (LLVMOperand) v.getTranslation(expr);
            LLVMTypeNode llvmType =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, expr.type());
            v.addTranslation(n, nf.LLVMRet(llvmType, o));
        }

        return super.translatePseudoLLVM(v);
    }
}
