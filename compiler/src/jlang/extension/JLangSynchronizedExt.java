//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;

public class JLangSynchronizedExt extends JLangExt {
    private static boolean printedWarning = false;

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        // TODO
        if (!printedWarning) {
            System.err.println("WARNING: synchronized keyword unimplemented.");
            printedWarning = true;
        }
        return super.leaveTranslateLLVM(v);
    }
}
