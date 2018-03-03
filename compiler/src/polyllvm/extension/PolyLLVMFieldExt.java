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
        } else {
            LLVMValueRef x_target = v.getTranslation(n.target());
            int offset = v.fieldInfo(n.target().type().toReference(), fi);
            // Make sure the LLVM type of the receiver object is not opaque before GEP occurs.
            v.utils.toLL(n.target().type());
            LLVMValueRef val = v.utils.buildStructGEP(x_target, 0, offset);
            // Bitcast needed due to potential mismatch introduced by erasure.
            return v.utils.toBitcastL(val, n.type());
        }
    }
}
