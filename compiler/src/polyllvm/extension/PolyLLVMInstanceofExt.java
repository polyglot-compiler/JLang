package polyllvm.extension;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMInstanceofExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Instanceof n = (Instanceof) node();
        LLVMValueRef obj =  v.getTranslation(n.expr());
        ReferenceType compareRt = n.compareType().type().toReference();
        LLVMValueRef compTypeIdVar = ClassObjects.classIdVarRef(v.mod, compareRt);
        LLVMTypeRef bytePtrType = LLVMUtils.ptrTypeRef(LLVMInt8Type());

        // Cast obj to a byte pointer.
        LLVMValueRef objBitcast = LLVMBuildBitCast(v.builder, obj, bytePtrType, "cast_obj_byte_ptr");

        // Build call to native code.
        LLVMValueRef function = LLVMUtils.getFunction(v.mod, "instanceof",
                LLVMUtils.functionType(LLVMInt1Type(), bytePtrType, bytePtrType));
        LLVMValueRef result = LLVMUtils.buildMethodCall(v.builder, function, objBitcast, compTypeIdVar);

        v.addTranslation(n, result);
        return super.translatePseudoLLVM(v);
    }
}
