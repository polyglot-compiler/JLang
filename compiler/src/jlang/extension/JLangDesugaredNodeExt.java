//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;

public class JLangDesugaredNodeExt extends JLangExt {
    private static final String errorMsg =
            "This node should be desugared before translating to LLVM IR";

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        throw new InternalCompilerError(errorMsg + ": " + node().getClass());
    }
}
