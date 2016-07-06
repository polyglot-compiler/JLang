package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public interface LLVMSub extends LLVMBinaryOperandInstruction {
    /**
     * Return a new LLVMSub with the result variable {@code o}
     */
    @Override
    LLVMSub result(LLVMVariable o);

    /**
     * Return a new LLVMSub with the new result type being {@code i}
     */
    LLVMSub intType(LLVMIntType i);

    /**
     * Return a new LLVMSub with the new left operand being {@code l}
     */
    @Override
    LLVMSub left(LLVMOperand l);

    /**
     * Return a new LLVMSub with the new right operand being {@code r}
     */
    @Override
    LLVMSub right(LLVMOperand r);

    /**
     * @return The int type of this instruction
     */
    LLVMIntType intType();

    /**
     * @return The left operand of this instruction
     */
    @Override
    LLVMOperand left();

    /**
     * @return The right operand of this instruction
     */
    @Override
    LLVMOperand right();
}
