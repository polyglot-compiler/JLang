package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMBinaryOperandInstruction extends LLVMInstruction {
    /**
     * Return a new LLVMBinaryOperandInstruction with the result variable {@code o}
     */
    @Override
    LLVMBinaryOperandInstruction result(LLVMVariable o);

    /**
     * Return a new LLVMBinaryOperandInstruction with the new result type
     * being {@code tn}
     */
    LLVMBinaryOperandInstruction typeNode(LLVMTypeNode tn);

    /**
     * Return a new LLVMBinaryOperandInstruction with the new left operand
     * being {@code l}
     */
    LLVMBinaryOperandInstruction left(LLVMOperand l);

    /**
     * Return a new LLVMBinaryOperandInstruction with the new right operand
     * being {@code r}
     */
    LLVMBinaryOperandInstruction right(LLVMOperand r);

    /**
     * @return The type node of this instruction
     */
    LLVMTypeNode typeNode();

    /**
     * @return The left operand of this instruction
     */
    LLVMOperand left();

    /**
     * @return The right operand of this instruction
     */
    LLVMOperand right();

}
