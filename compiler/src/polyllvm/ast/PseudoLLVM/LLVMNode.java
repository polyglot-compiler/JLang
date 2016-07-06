package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Node;
import polyllvm.visit.RemoveESeqVisitor;

/**
 * A Pseudo LLVM node for constructing LLVM code. May not be valid LLVM IR
 * @author Daniel
 */
public interface LLVMNode extends Node {

    /**
     * Remove ESEQ nodes
     */
    public LLVMNode removeESeq(RemoveESeqVisitor v);
}
