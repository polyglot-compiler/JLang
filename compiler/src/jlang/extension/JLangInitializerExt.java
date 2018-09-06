//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

public class JLangInitializerExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        // Instance initializers and static initializers are desugared
        // into standalone functions that are called when necessary.
        return node();
    }
}
