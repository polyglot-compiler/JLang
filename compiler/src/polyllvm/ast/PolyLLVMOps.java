package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    Node removeStringLiterals(StringLiteralRemover v);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVM(PseudoLLVMTranslator v);

    Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel);

    /**
     * Adds the conditional translation of this node to the current block. If this node
     * evaluates to true, jump to {@code trueBlock}, otherwise jump to
     * {@code falseBlock}. Creates additional blocks if needed.
     * Does not switch the current block of v.
     * @param v
     * @param trueBlock
     * @param falseBlock
     */
    void translateLLVMConditional(PseudoLLVMTranslator v,
                                               LLVMBasicBlockRef trueBlock,
                                               LLVMBasicBlockRef falseBlock);

    AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(
            AddPrimitiveWideningCastsVisitor v);

    Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v);
}
