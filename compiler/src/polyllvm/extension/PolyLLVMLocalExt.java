package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.types.LocalInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMLocalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Local n = (Local) node();
        LLVMValueRef ptr = translateAsLValue(v);
        LLVMValueRef val = LLVMBuildLoad(v.builder, ptr, "load." + n.name());
        v.addTranslation(n, val);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Local n = (Local) node();
        LocalInstance li = n.localInstance().orig();
        return v.getTranslation(li);
    }
}
