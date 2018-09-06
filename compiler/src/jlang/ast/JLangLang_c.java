//Copyright (C) 2018 Cornell University

package jlang.ast;

import polyglot.ast.Ext;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ext.jl7.ast.J7Lang_c;
import polyglot.util.InternalCompilerError;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

public class JLangLang_c extends J7Lang_c implements JLangLang {
    public static final JLangLang_c instance = new JLangLang_c();

    public static JLangLang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof JLangLang) return (JLangLang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }


    protected JLangLang_c() {
    }

    protected static JLangExt jlangExt(Node n) {
        return JLangExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jlangExt(n);
    }

    protected JLangOps JLangOps(Node n) {
        return jlangExt(n);
    }

    @Override
    public Node desugar(Node n, DesugarLocally v) {
        return JLangOps(n).desugar(v);
    }

    @Override
    public LLVMTranslator enterTranslateLLVM(Node n, LLVMTranslator v) {
        return JLangOps(n).enterTranslateLLVM(v);
    }

    @Override
    public Node leaveTranslateLLVM(Node n, LLVMTranslator v) {
        return JLangOps(n).leaveTranslateLLVM(v);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, Node n, LLVMTranslator v) {
        return JLangOps(n).overrideTranslateLLVM(parent, v);
    }

    @Override
    public void translateLLVMConditional(Node n, LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        JLangOps(n).translateLLVMConditional(v, trueBlock, falseBlock);
    }

    @Override
    public LLVMValueRef translateAsLValue(Node n, LLVMTranslator v) {
        return JLangOps(n).translateAsLValue(v);
    }
}
