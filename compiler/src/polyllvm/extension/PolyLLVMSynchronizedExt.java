package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMSynchronizedExt extends PolyLLVMExt {
    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("synchronized blocks unimplemented");
    }
}
