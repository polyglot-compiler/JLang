package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;

public interface LLVMGetElementPtr extends LLVMInstruction {
    @Override
    LLVMGetElementPtr result(LLVMVariable o);
}
