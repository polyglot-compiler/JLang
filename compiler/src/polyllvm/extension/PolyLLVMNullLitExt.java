package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMConstNull;

public class PolyLLVMNullLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        NullLit n = (NullLit) node();
        v.debugInfo.emitLocation(n);
        v.addTranslation(n, LLVMConstNull(v.utils.typeRef(n.type())));
        return super.leaveTranslateLLVM(v);
    }
}
