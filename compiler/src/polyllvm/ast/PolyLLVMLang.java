package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ext.jl7.ast.J7Lang;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.DesugarLocally;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public interface PolyLLVMLang extends J7Lang {

    /**
     * Simplifies (desugars) this node so that translation to LLVM is easier.
     * This method should be called until the AST reaches a fixed point.
     */
    Node desugar(Node n, DesugarLocally v);

    Node overrideTranslateLLVM(Node parent, Node n, LLVMTranslator LLVMTranslator);

    LLVMTranslator enterTranslateLLVM(Node n, LLVMTranslator v);

    Node leaveTranslateLLVM(Node n, LLVMTranslator v);

    void translateLLVMConditional(Node n, LLVMTranslator v,
                                  LLVMBasicBlockRef trueBlock,
                                  LLVMBasicBlockRef falseBlock);

    LLVMValueRef translateAsLValue(Node n, LLVMTranslator v);
}
