package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public interface LLVMAdd extends LLVMBinaryOperandInstruction {
    /**
     * Return a new LLVMAdd with the result variable {@code o}
     */
    @Override
    LLVMAdd result(LLVMVariable o);

    /**
     * Return a new LLVMAdd with the new result type being {@code i}
     */
    LLVMAdd intType(LLVMIntType i);

    /**
     * Return a new LLVMAdd with the new left operand being {@code l}
     */
    @Override
    LLVMAdd left(LLVMOperand l);

    /**
     * Return a new LLVMAdd with the new right operand being {@code r}
     */
    @Override
    LLVMAdd right(LLVMOperand r);

    /**
     * @return The int type of this instruction
     */
    LLVMIntType intType();
}
