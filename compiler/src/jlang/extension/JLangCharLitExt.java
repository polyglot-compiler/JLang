//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.CharLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.*;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangCharLitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        CharLit n = (CharLit) node();
        LLVMTypeRef type = v.utils.toLL(n.type());
        LLVMValueRef val = LLVMConstInt(type, n.value(), /*sign-extend*/ 0);
        v.addTranslation(n, val);
        return super.leaveTranslateLLVM(v);
    }
}
