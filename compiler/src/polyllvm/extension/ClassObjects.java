package polyllvm.extension;

import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Creates LLVM IR for class objects.
 */
public final class ClassObjects {

    public ClassObjects() {}

    public static LLVMTypeRef classIdVarTypeRef() {
        return LLVMInt8Type();
    }

    public static LLVMTypeRef classIdVarPtrTypeRef() {
        return  LLVMUtils.ptrTypeRef(classIdVarTypeRef());
    }

    public static LLVMValueRef classIdDeclRef(LLVMModuleRef mod,
                                                       ReferenceType rt,
                                                       boolean extern) {
        LLVMValueRef global = LLVMUtils.getGlobal(mod, PolyLLVMMangler.classIdName(rt), classIdVarTypeRef());
        if(!extern){
            LLVMSetInitializer(global, LLVMConstInt(LLVMInt8Type(), 0, /*sign-extend*/ 0));
        }
        return global;
    }

    public static LLVMValueRef classIdVarRef(LLVMModuleRef mod, ReferenceType rt) {
        return LLVMUtils.getGlobal(mod, PolyLLVMMangler.classIdName(rt), classIdVarTypeRef());
    }

    public static LLVMValueRef classObjRef(LLVMModuleRef mod, ReferenceType rt) {
        List<LLVMValueRef> classObjPtrOperands = new ArrayList<>();

        Type superType = rt;
        while (superType != null) {
            classObjPtrOperands.add(classIdVarRef(mod, superType.toReference()));
            superType = superType.toReference().superType();
        }

        rt.interfaces().stream().map(it -> classIdVarRef(mod, it))
                .forEach(classObjPtrOperands::add);

        LLVMValueRef classObjPtrs = LLVMUtils.buildConstArray(classIdVarPtrTypeRef(), classObjPtrOperands.toArray(new LLVMValueRef[1]));
        LLVMValueRef numSupertypes = LLVMConstInt(LLVMInt32Type(), countSupertypes(rt), /*sign-extend*/ 0);
        LLVMValueRef classObjStruct = LLVMUtils.buildConstStruct(numSupertypes, classObjPtrs);

        LLVMValueRef global = LLVMUtils.getGlobal(mod, PolyLLVMMangler.classObjName(rt), LLVMTypeOf(classObjStruct));
        LLVMSetExternallyInitialized(global, 0);
        LLVMSetInitializer(global, classObjStruct);

        return global;
    }

    /** Counts the supertypes for this reference type, including itself. */
    public static int countSupertypes(ReferenceType rt) {
        int ret = 0;
        for (Type s = rt; s != null; s = s.toReference().superType())
            ++ret;
        ret += rt.interfaces().size();
        return ret;
    }

    public static LLVMTypeRef classObjArrTypeRef(ReferenceType rt) {
        return LLVMArrayType(classIdVarPtrTypeRef(), countSupertypes(rt));
    }

    public static LLVMTypeRef classObjTypeRef(ReferenceType rt) {
        return LLVMUtils.structType(LLVMInt32Type(), classObjArrTypeRef(rt));
    }
}
