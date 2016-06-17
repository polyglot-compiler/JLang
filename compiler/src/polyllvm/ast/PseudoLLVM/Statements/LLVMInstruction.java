package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;

/**
 * @author Daniel
 *
 */
public interface LLVMInstruction extends LLVMNode {

    /**
     * Return a new LLVMInstruction with the result variable {@code o}
     */
    LLVMInstruction result(LLVMVariable o);

}
