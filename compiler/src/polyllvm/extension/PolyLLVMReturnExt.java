package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

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
