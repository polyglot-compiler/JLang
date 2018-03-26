package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMSynchronizedExt extends PolyLLVMExt {
    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        // TODO
        System.err.println("WARNING: synchronized keyword unimplemented.");
        return super.leaveTranslateLLVM(v);
    }
}
