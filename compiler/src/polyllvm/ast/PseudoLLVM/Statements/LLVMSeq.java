package polyllvm.ast.PseudoLLVM.Statements;

import java.util.List;

public interface LLVMSeq extends LLVMInstruction {
    public List<LLVMInstruction> instructions();
}
