package polyllvm.ast.PseudoLLVM.Expressions;

import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMTypedOperand extends LLVMOperand {

    /**
     * Return the operand without its type
     */
    LLVMOperand operand();

    /**
     * Return the type node of this operand
     */
    LLVMTypeNode typeNode();
}
