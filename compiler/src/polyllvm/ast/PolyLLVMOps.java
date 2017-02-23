package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVM(PseudoLLVMTranslator v);

    Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v);

    /**
     * Adds the conditional translation of this node to the current block. If this node
     * evaluates to true, jump to {@code trueBlock}, otherwise jump to
     * {@code falseBlock}. Creates additional blocks if needed.
     */
    void translateLLVMConditional(PseudoLLVMTranslator v,
                                  LLVMBasicBlockRef trueBlock,
                                  LLVMBasicBlockRef falseBlock);
}
