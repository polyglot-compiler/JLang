package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMSynchronizedExt extends PolyLLVMExt {
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
