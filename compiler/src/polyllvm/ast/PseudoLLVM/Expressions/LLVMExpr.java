package polyllvm.ast.PseudoLLVM.Expressions;

import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMExpr extends LLVMNode {

    LLVMTypeNode typeNode();

}
