package polyllvm.ast;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyglot.ext.jl7.ast.J7Lang;
import polyllvm.visit.LLVMTranslator;

public interface PolyLLVMLang extends J7Lang {
    // TODO: Declare any dispatch methods for new AST operations

    LLVMTranslator enterTranslatePseudoLLVM(Node n,
                                            LLVMTranslator v);

    Node translatePseudoLLVM(Node n, LLVMTranslator v);

    Node overrideTranslatePseudoLLVM(Node n, LLVMTranslator LLVMTranslator);

    void translateLLVMConditional(Node n, LLVMTranslator v,
                                  LLVM.LLVMBasicBlockRef trueBlock,
                                  LLVM.LLVMBasicBlockRef falseBlock);
}
