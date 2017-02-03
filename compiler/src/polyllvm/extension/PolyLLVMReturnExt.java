package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Return n = (Return) node();
        NodeFactory nf = v.nodeFactory();
        if (v.getCurrentMethod() instanceof MethodDecl) {
            TypeNode returnType = ((MethodDecl) v.getCurrentMethod()).returnType();
            if (n.expr() != null && !returnType.type().equals(n.expr().type())) {
                Expr cast = nf.Cast(Position.compilerGenerated(), returnType, n.expr())
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
        Expr e = n.expr();
        LLVMValueRef res = e == null
                ? LLVMBuildRetVoid(v.builder)
                : LLVMBuildRet(v.builder, v.getTranslation(e));
        v.addTranslation(n, res);
        return super.translatePseudoLLVM(v);
    }
}
