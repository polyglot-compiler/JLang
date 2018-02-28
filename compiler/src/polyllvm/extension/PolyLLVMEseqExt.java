package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMEseqExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        return super.leaveTranslateLLVM(v);
    }
}
