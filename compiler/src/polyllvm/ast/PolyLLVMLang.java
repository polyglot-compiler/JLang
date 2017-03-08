package polyllvm.ast;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Node;
import polyglot.ext.jl7.ast.J7Lang;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public interface PolyLLVMLang extends J7Lang {

    LLVMTranslator enterTranslatePseudoLLVM(Node n,
                                            LLVMTranslator v);

    Node translatePseudoLLVM(Node n, LLVMTranslator v);

    Node overrideTranslatePseudoLLVM(Node n, LLVMTranslator LLVMTranslator);

    void translateLLVMConditional(Node n, LLVMTranslator v,
                                  LLVM.LLVMBasicBlockRef trueBlock,
                                  LLVM.LLVMBasicBlockRef falseBlock);

    LLVMValueRef translateAsLValue(Node n, LLVMTranslator v);
}
