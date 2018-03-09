package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.types.FieldInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Field n = (Field) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug info.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "load." + n.name());
        v.addTranslation(n, load);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Field n = (Field) node();
        FieldInstance fi = n.fieldInstance();
        n.visitChild(n.target(), v);

        if (n.flags().isStatic()) {
            String mangledGlobalName = v.mangler.mangleStaticFieldName(fi);
            LLVMTypeRef elemType = v.utils.toLL(n.type());
            return v.utils.getGlobal(mangledGlobalName, elemType);
        }
        else {
            LLVMValueRef instance = v.getTranslation(n.target());
            LLVMValueRef ptr = v.obj.buildFieldElementPtr(instance, fi);

            // Bitcast needed due to potential mismatch introduced by erasure.
            return v.utils.toBitcastL(ptr, n.type());
        }
    }
}
