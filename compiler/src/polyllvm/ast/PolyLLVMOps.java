package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    LLVMTranslator enterTranslatePseudoLLVM(LLVMTranslator v);

    Node translatePseudoLLVM(LLVMTranslator v);

    Node overrideTranslatePseudoLLVM(LLVMTranslator v);

    /**
     * Adds the conditional translation of this node to the current block. If this node
     * evaluates to true, jump to {@code trueBlock}, otherwise jump to
     * {@code falseBlock}. Creates additional blocks if needed.
     */
    void translateLLVMConditional(LLVMTranslator v,
                                  LLVMBasicBlockRef trueBlock,
                                  LLVMBasicBlockRef falseBlock);
}
