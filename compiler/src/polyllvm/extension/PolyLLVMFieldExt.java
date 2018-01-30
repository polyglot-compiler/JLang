package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.FieldInstance;
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
        FieldInstance fi = n.fieldInstance();
        target.visit(v);
        v.debugInfo.emitLocation(n);

        if (n.flags().isStatic()) {
            String mangledGlobalName = v.mangler.mangleStaticFieldName(fi);
            LLVMTypeRef elemType = v.utils.toLL(n.type());
            return v.utils.getGlobal(v.mod, mangledGlobalName, elemType);
        } else {
            LLVMValueRef x_target = v.getTranslation(target);
            int offset = v.fieldInfo(((Expr) target).type().toReference(), fi);
            // Make sure the LLVM type of the receiver object is not opaque
            // before GEP occurs
            v.utils.toLL(target.type());
            LLVMValueRef val = v.utils.buildStructGEP(x_target, 0, offset);
            // bitcast needed due to potential mismatch introduced by erasure
            return v.utils.toBitcastL(val, n.type());
        }
    }
}
