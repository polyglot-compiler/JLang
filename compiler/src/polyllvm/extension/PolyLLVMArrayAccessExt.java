package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug location.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "arr_load");
        v.addTranslation(n, load);
        return n;
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        // Return a pointer to the appropriate element in the array.
        ArrayAccess n = (ArrayAccess) node();
        n.array().visit(v);
        n.index().visit(v);
        v.debugInfo.emitLocation(n);
        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef base = v.utils.buildJavaArrayBase(arr, n.type());
        LLVMValueRef offset = v.getTranslation(n.index());
        return v.utils.buildGEP(v.builder, base, offset);
    }
}
