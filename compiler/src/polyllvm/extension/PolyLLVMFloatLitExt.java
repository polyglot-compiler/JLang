package polyllvm.extension;

import polyglot.ast.FloatLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMConstReal;
import static org.bytedeco.javacpp.LLVM.LLVMTypeRef;

public class PolyLLVMFloatLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        FloatLit n = (FloatLit) node();
        v.debugInfo.emitLocation(n);
        LLVMTypeRef type = v.utils.typeRef(n.type());
        v.addTranslation(n, LLVMConstReal(type, n.value()));
        return super.leaveTranslateLLVM(v);
    }
}
