package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public interface LLVMMul extends LLVMBinaryOperandInstruction {
    /**
     * Return a new LLVMMul with the result variable {@code o}
     */
    @Override
    LLVMMul result(LLVMVariable o);

    /**
     * Return a new LLVMMul with the new result type being {@code i}
     */
    LLVMMul intType(LLVMIntType i);

    /**
     * Return a new LLVMMul with the new left operand being {@code l}
     */
    @Override
    LLVMMul left(LLVMOperand l);

    /**
     * Return a new LLVMMul with the new right operand being {@code r}
     */
    @Override
    LLVMMul right(LLVMOperand r);

    /**
     * @return The int type of this instruction
     */
    LLVMIntType intType();
}
