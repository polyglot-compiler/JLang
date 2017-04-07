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
        LLVMValueRef compTypeIdVar = v.classObjs.classIdVarRef(rt);
        LLVMTypeRef bytePtrType = v.utils.ptrTypeRef(v.utils.intType(8));
        LLVMValueRef objBitcast = LLVMBuildBitCast(v.builder, obj, bytePtrType, "cast_obj");
        LLVMTypeRef funcType = v.utils.functionType(v.utils.intType(1), bytePtrType, bytePtrType);
        LLVMValueRef function = v.utils.getFunction(v.mod, "instanceof", funcType);
        return v.utils.buildMethodCall(function, objBitcast, compTypeIdVar);
    }
}
