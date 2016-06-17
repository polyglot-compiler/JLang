package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;

public interface LLVMCall extends LLVMInstruction {

    @Override
    LLVMCall result(LLVMVariable o);

}
