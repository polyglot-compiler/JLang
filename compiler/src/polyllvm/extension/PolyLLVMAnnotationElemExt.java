package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMAnnotationElemExt extends PolyLLVMExt {

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        // Do not recurse into annotations, since there may actually be
        // expressions inside, and we don't want to translate them.
        // E.g., the annotation @Target({FIELD, METHOD}) contains a list
        // of enum values, but they should not appear in LLVM IR.
        return node();
    }
}
