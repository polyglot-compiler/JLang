//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.Node;

import java.lang.Override;

import jlang.ast.Load;
import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangLoadExt extends JLangExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Load n = (Load) node();
        LLVMValueRef ptr = v.getTranslation(n.expr());
        LLVMValueRef val = LLVMBuildLoad(v.builder, ptr, "load");
        v.addTranslation(n, val);
        return super.leaveTranslateLLVM(v);
    }
}
