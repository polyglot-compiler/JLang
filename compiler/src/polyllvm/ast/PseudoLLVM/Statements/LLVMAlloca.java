package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;

public interface LLVMAlloca extends LLVMInstruction {
    @Override
    LLVMAlloca result(LLVMVariable o);
}
