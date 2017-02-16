package polyllvm.ast;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public interface PolyLLVMLang extends JLang {
    // TODO: Declare any dispatch methods for new AST operations

    Node removeStringLiterals(Node n, StringLiteralRemover llvmTranslation);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(Node n,
            PseudoLLVMTranslator v);

    Node translatePseudoLLVM(Node n, PseudoLLVMTranslator v);

    Node overrideTranslatePseudoLLVM(Node n, PseudoLLVMTranslator pseudoLLVMTranslator);

    void translateLLVMConditional(Node n, PseudoLLVMTranslator v,
                                  LLVM.LLVMBasicBlockRef trueBlock,
                                  LLVM.LLVMBasicBlockRef falseBlock);
}
