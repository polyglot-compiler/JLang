package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        Field n = (Field) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug info.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "load_field");
        v.addTranslation(n, load);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Field n = (Field) node();
        Receiver target = n.target();
        target.visit(v);
        v.debugInfo.emitLocation(n);

        if (n.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(n);
            LLVMTypeRef type = v.utils.ptrTypeRef(v.utils.typeRef(n.type()));
            return v.utils.getGlobal(v.mod, mangledGlobalName, type);
        } else {
            // Instance fields.
            LLVMValueRef thisTranslation = v.getTranslation(target);
            int fieldIndex = v.getFieldIndex((ReferenceType) n.target().type(), n.fieldInstance());
            return v.utils.buildStructGEP(v.builder, thisTranslation, 0, fieldIndex);
        }
    }
}
