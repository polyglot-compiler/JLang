package polyllvm.ast.PseudoLLVM.Statements;

import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

/**
 * @author Daniel
 *
 */
public interface LLVMInstruction extends LLVMNode {

    /**
     * Return a new LLVMInstruction with the result variable {@code o}
     */
    LLVMInstruction result(LLVMVariable o);

    /**
     * @return The result variable of this instruction
     */
    LLVMVariable result();

    /**
     * The type that this instruction returns
     */
    LLVMTypeNode retType();
//
//    /**
//     * Add the comment {@code s} to the instruction
//     */
//    LLVMInstruction comment(String s);

}
