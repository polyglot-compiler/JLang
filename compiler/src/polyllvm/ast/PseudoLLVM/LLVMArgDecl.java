package polyllvm.ast.PseudoLLVM;

import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMArgDecl extends LLVMNode {

    /**
     * Return the name of the variable of this argument declaration.
     */
    String varName();

    /**
     * Return the type node of this argument
     */
    LLVMTypeNode typeNode();

}
