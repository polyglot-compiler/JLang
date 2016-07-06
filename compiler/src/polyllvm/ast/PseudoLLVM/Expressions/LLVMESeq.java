package polyllvm.ast.PseudoLLVM.Expressions;

import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;

public interface LLVMESeq extends LLVMOperand {

    /**
     * Return the instruction of this ESeq
     */
    LLVMInstruction instruction();

    /**
     * Return the expression of this ESeq
     */
    LLVMOperand expr();

    /**
     * Return a new ESEQ with instruction {@code i}
     */
    LLVMESeq instruction(LLVMInstruction i);

    /**
     * Return a new ESEQ with expression {@code e}
     */
    LLVMESeq expr(LLVMOperand e);
}
