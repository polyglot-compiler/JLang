package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Field n = (Field) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug info.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "load_field");
        v.addTranslation(n, load);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Field n = (Field) node();
        Receiver target = n.target();
        target.visit(v);
        v.debugInfo.emitLocation(n);

        if (n.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = v.mangler.mangleStaticFieldName(n);
            LLVMTypeRef elemType = v.utils.typeRef(n.type());
            return v.utils.getGlobal(v.mod, mangledGlobalName, elemType);
        } else {
            // Instance fields.
            v.utils.typeRef(target.type()); // Ensure the type body is set before GEP
            LLVMValueRef thisTranslation = v.getTranslation(target);
            int fieldIndex = v.getFieldIndex((ReferenceType) n.target().type(), n.fieldInstance());
            return v.utils.buildStructGEP(thisTranslation, 0, fieldIndex);
        }
    }
}
