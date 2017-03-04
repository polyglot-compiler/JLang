package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Field n = (Field) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Receiver target = n.target();

        v.debugInfo.emitLocation(n);

        if (n.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(n);
            LLVMValueRef global = v.utils.getGlobal(v.mod, mangledGlobalName, v.utils.ptrTypeRef(v.utils.typeRef(n.type())));
            v.addTranslation(n, LLVMBuildLoad(v.builder, global, "static_field_access"));
        }
        else {
            // Instance fields.
            LLVMValueRef thisTranslation = v.getTranslation(target);
            int fieldIndex = v.getFieldIndex((ReferenceType) n.target().type(), n.fieldInstance());
            LLVMValueRef gep = v.utils.buildGEP(v.builder, thisTranslation,
                    LLVMConstInt(LLVMInt32TypeInContext(v.context), 0, 0), LLVMConstInt(LLVMInt32TypeInContext(v.context), fieldIndex, 0));
            v.addTranslation(n, LLVMBuildLoad(v.builder, gep, "load_field"));

        }

        return super.translatePseudoLLVM(v);
    }
}
