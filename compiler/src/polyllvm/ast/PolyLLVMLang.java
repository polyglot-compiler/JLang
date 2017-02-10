package polyllvm.ast;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.*;

public interface PolyLLVMLang extends JLang {
    // TODO: Declare any dispatch methods for new AST operations

    Node removeStringLiterals(Node n, StringLiteralRemover llvmTranslation);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(Node n,
            PseudoLLVMTranslator v);

    Node translatePseudoLLVM(Node n, PseudoLLVMTranslator v);

    Node overrideTranslatePseudoLLVM(Node n, PseudoLLVMTranslator pseudoLLVMTranslator);

    Node translatePseudoLLVMConditional(Node n, PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel);

    void translateLLVMConditional(Node n, PseudoLLVMTranslator v,
                                  LLVM.LLVMBasicBlockRef trueBlock,
                                  LLVM.LLVMBasicBlockRef falseBlock);

    Node removeESeq(Node n, RemoveESeqVisitor removeESeqVisitor);

    AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(Node n,
            AddPrimitiveWideningCastsVisitor v);

    Node addPrimitiveWideningCasts(Node n, AddPrimitiveWideningCastsVisitor v);
}
