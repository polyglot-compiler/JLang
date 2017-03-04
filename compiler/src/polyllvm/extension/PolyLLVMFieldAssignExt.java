package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        FieldAssign n = (FieldAssign) node();
        Field field = n.left();
        Receiver objectTarget = field.target();
        LLVMValueRef expr = v.getTranslation(n.right());
        LLVMTypeRef fieldTypeRef = v.utils.typeRef(field.type());

        v.debugInfo.emitLocation(n);

        if (field.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(field);
            LLVMValueRef global = v.utils.getGlobal(v.mod, mangledGlobalName, fieldTypeRef);
            LLVMValueRef store = LLVMBuildStore(v.builder, expr, global);
            v.addTranslation(n, store);
        }
        else {
            // Instance fields.
            LLVMValueRef objectTranslation = v.getTranslation(objectTarget);
            int fieldIndex = v.getFieldIndex((ReferenceType) objectTarget.type(),
                    field.fieldInstance());
            LLVMValueRef gep = v.utils.buildGEP(v.builder,objectTranslation,
                    LLVMConstInt(LLVMInt32TypeInContext(v.context), 0, /*sign extend*/ 0),
                    LLVMConstInt(LLVMInt32TypeInContext(v.context), fieldIndex, /*sign extend*/ 0));
            v.addTranslation(n, LLVMBuildStore(v.builder, expr, gep));
        }

        return super.translatePseudoLLVM(v);
    }

}
