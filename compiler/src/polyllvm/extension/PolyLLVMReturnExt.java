package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
//        if(node().)
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
