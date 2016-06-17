package polyllvm.ast.PseudoLLVM;

import java.util.List;

import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;

/**
 * @author Daniel
 *
 */
public interface LLVMBlock extends LLVMNode {

    /**
     * Return a new LLVMBlock with the code replaced with {@code instructions}
     */
    LLVMBlock instructions(List<LLVMInstruction> instructions);

    /**
     * Return a new LLVMBlock with {@code i} appended to the end of
     * the current block's instructions
     */
    LLVMBlock appendInstruction(LLVMInstruction i);

}
