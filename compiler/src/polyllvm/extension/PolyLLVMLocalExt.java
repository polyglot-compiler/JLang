package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.types.PolyLLVMLocalInstance;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMLocalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Local n = (Local) node();
        PolyLLVMLocalInstance li = (PolyLLVMLocalInstance) n.localInstance().orig();

        if (li.isSSA()) {
            // SSA values are already ready to use.
            v.addTranslation(n, v.getTranslation(li));
        }
        else {
            // Otherwise, we need to load from the stack.
            LLVMValueRef ptr = translateAsLValue(v);
            LLVMValueRef val = LLVMBuildLoad(v.builder, ptr, "load." + n.name());
            v.addTranslation(n, val);
        }

        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Local n = (Local) node();
        PolyLLVMLocalInstance li = (PolyLLVMLocalInstance) n.localInstance().orig();
        if (li.isSSA())
            throw new InternalCompilerError("Attempting to translate SSA variable as pointer");
        return v.getTranslation(li);
    }
}
