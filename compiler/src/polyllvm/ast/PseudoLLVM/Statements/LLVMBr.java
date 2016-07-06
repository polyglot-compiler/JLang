package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;

public interface LLVMBr extends LLVMInstruction {

    /**
     * Return the condition associated with this br, null if unconditional jump.
     */
    LLVMTypedOperand cond();

    /**
     * Return a new br instruction with condition {@code temp}
     */
    LLVMBr cond(LLVMTypedOperand temp);
}
