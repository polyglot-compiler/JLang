package jlang.extension;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;

public class JLangAnnotationElemExt extends JLangExt {

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        // Do not recurse into annotations, since there may actually be
        // expressions inside, and we don't want to translate them.
        // E.g., the annotation @Target({FIELD, METHOD}) contains a list
        // of enum values, but they should not appear in LLVM IR.
        return node();
    }
}
