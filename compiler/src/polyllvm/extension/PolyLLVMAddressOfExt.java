package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.AddressOf;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMAddressOfExt extends PolyLLVMExt {

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        AddressOf n = (AddressOf) node();
        LLVMValueRef ptr = lang().translateAsLValue(n.expr(), v);
        v.addTranslation(n, ptr);
        return super.leaveTranslateLLVM(v);
    }
}
