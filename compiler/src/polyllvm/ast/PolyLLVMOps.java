package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.LLVMVarToStack;
import polyllvm.visit.PrintVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.RemoveESeqVisitor;
import polyllvm.visit.StringLiteralRemover;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    Node print(PrintVisitor v);

    Node removeStringLiterals(StringLiteralRemover v);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel);

    Node addVoidReturn(AddVoidReturnVisitor v);

    Node llvmVarToStack(LLVMVarToStack v);

    Node removeESeq(RemoveESeqVisitor v);

    Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v);

}
