//Copyright (C) 2017 Cornell University

package jlang.ast;

import polyglot.ast.Node;
import polyglot.ext.jl7.ast.J7Lang;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

public interface JLangLang extends J7Lang {

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
