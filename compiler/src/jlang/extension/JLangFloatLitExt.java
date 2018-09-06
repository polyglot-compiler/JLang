//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.FloatLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.LLVMConstReal;
import static org.bytedeco.javacpp.LLVM.LLVMTypeRef;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangFloatLitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        FloatLit n = (FloatLit) node();
        LLVMTypeRef type = v.utils.toLL(n.type());
        v.addTranslation(n, LLVMConstReal(type, n.value()));
        return super.leaveTranslateLLVM(v);
    }
}
