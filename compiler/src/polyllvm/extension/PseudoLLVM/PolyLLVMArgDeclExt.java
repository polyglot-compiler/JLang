package polyllvm.extension.PseudoLLVM;

import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.visit.LLVMVarToStack;

public class PolyLLVMArgDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node llvmVarToStack(LLVMVarToStack v) {
        LLVMArgDecl n = (LLVMArgDecl) node();
        v.addArgument(n.varName(), n.typeNode());
        return super.llvmVarToStack(v);
    }

}
