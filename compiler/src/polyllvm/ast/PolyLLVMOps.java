package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.DesugarLocally;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    /**
     * Simplifies (desugars) this node so that translation to LLVM is easier.
     * This method will be called until the AST reaches a fixed point.
     */
    Node desugar(DesugarLocally v);

    Node overrideTranslateLLVM(Node parent, LLVMTranslator v);

    LLVMTranslator enterTranslateLLVM(LLVMTranslator v);

    Node leaveTranslateLLVM(LLVMTranslator v);

    /**
     * Adds the conditional translation of this node to the current block. If this node
     * evaluates to true, jump to {@code trueBlock}, otherwise jump to
     * {@code falseBlock}. Creates additional blocks if needed.
     */
    void translateLLVMConditional(LLVMTranslator v,
                                  LLVMBasicBlockRef trueBlock,
                                  LLVMBasicBlockRef falseBlock);

    /**
     * Translate this node as a pointer into which a value can be stored.
     * This only applies to nodes which could appear on the left side of an assignment expression.
     */
    LLVMValueRef translateAsLValue(LLVMTranslator v);
}
