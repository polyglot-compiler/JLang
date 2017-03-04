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
        ReferenceType compareRt = n.compareType().type().toReference();
        LLVMValueRef compTypeIdVar = v.classObjs.classIdVarRef(v.mod, compareRt);
        LLVMTypeRef bytePtrType = v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context));

        v.debugInfo.emitLocation(n);

        // Cast obj to a byte pointer.
        LLVMValueRef objBitcast = LLVMBuildBitCast(v.builder, obj, bytePtrType, "cast_obj_byte_ptr");

        // Build call to native code.
        LLVMValueRef function = v.utils.getFunction(v.mod, "instanceof",
                v.utils.functionType(LLVMInt1TypeInContext(v.context), bytePtrType, bytePtrType));
        LLVMValueRef result = v.utils.buildMethodCall(function, objBitcast, compTypeIdVar);

        v.addTranslation(n, result);
        return super.translatePseudoLLVM(v);
    }
}
