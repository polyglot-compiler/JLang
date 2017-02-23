package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Field n = (Field) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Receiver target = n.target();

        if (n.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(n);
            LLVMValueRef global = LLVMUtils.getGlobal(v.mod, mangledGlobalName, LLVMUtils.ptrTypeRef(LLVMUtils.typeRef(n.type(), v)));
            v.addTranslation(n, LLVMBuildLoad(v.builder, global, "static_field_access"));
        }
        else {
            // Instance fields.
            LLVMValueRef thisTranslation = v.getTranslation(target);
            int fieldIndex = v.getFieldIndex((ReferenceType) n.target().type(), n.fieldInstance());
            LLVMValueRef gep = LLVMUtils.buildGEP(v.builder, thisTranslation,
                    LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), fieldIndex, 0));
            v.addTranslation(n, LLVMBuildLoad(v.builder, gep, "load_field"));

        }

        return super.translatePseudoLLVM(v);
    }
}
