package polyllvm.extension;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMInstanceofExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Instanceof n = (Instanceof) node();
        LLVMValueRef obj =  v.getTranslation(n.expr());
        ReferenceType rt = n.compareType().type().toReference();
        v.debugInfo.emitLocation(n);
        LLVMValueRef res = buildInstanceOf(v, obj, rt);
        v.addTranslation(n, res);
        return super.translatePseudoLLVM(v);
    }

    static LLVMValueRef buildInstanceOf(LLVMTranslator v, LLVMValueRef obj, ReferenceType rt) {
        LLVMValueRef compTypeIdVar = v.classObjs.classIdVarRef(v.mod, rt);
        LLVMTypeRef bytePtrType = v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context));
        LLVMValueRef objBitcast = LLVMBuildBitCast(v.builder, obj, bytePtrType, "cast_obj_byte_ptr");
        LLVMTypeRef funcType =
                v.utils.functionType(LLVMInt1TypeInContext(v.context), bytePtrType, bytePtrType);
        LLVMValueRef function = v.utils.getFunction(v.mod, "instanceof", funcType);
        return v.utils.buildMethodCall(function, objBitcast, compTypeIdVar);
    }
}
