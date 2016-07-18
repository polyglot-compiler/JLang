package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Return;
import polyglot.ast.TypeNode;
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
        TypeNode returnType = v.getCurrentMethod().returnType();
        if (!returnType.type().equals(n.expr().type())) {
            Expr cast =
                    nf.Cast(Position.compilerGenerated(), returnType, n.expr())
                      .type(returnType.type());
            return n.expr(cast);
        }
        return super.addPrimitiveWideningCasts(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Return n = (Return) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Expr expr = n.expr();
        if (expr == null) {
            v.addTranslation(n, nf.LLVMRet(Position.compilerGenerated()));
        }
        else {
            LLVMOperand o = (LLVMOperand) v.getTranslation(expr);
            LLVMTypeNode llvmType =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, expr.type());
            v.addTranslation(n,
                             nf.LLVMRet(Position.compilerGenerated(),
                                        llvmType,
                                        o));
        }

        return super.translatePseudoLLVM(v);
    }
}
