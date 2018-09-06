//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.LLVMConstNull;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangNullLitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        NullLit n = (NullLit) node();
        v.addTranslation(n, LLVMConstNull(v.utils.toLL(n.type())));
        return super.leaveTranslateLLVM(v);
    }
}
