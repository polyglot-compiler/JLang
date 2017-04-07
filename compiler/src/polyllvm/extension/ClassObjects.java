package polyllvm.extension;

import polyglot.types.ReferenceType;
import polyllvm.visit.LLVMTranslator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Creates LLVM IR for class objects.
 */
public final class ClassObjects {
    private final LLVMTranslator v;

    public ClassObjects(LLVMTranslator v) {
        this.v = v;
    }

    public LLVMTypeRef classIdVarTypeRef() {
        return LLVMInt8TypeInContext(v.context);
    }

    public LLVMTypeRef classIdVarPtrTypeRef() {
        return v.utils.ptrTypeRef(classIdVarTypeRef());
    }

    public LLVMValueRef classIdDeclRef(ReferenceType rt, boolean extern) {
        rt = v.jl5Utils.translateType(rt);
        LLVMValueRef global = v.utils.getGlobal(v.mod, v.mangler.classIdName(rt), classIdVarTypeRef());
        if (!extern) {
            LLVMSetInitializer(global, LLVMConstInt(LLVMInt8TypeInContext(v.context), 0, /*sign-extend*/ 0));
        }
        return global;
    }

    public LLVMValueRef classIdVarRef(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);
        return v.utils.getGlobal(v.mod, v.mangler.classIdName(rt), classIdVarTypeRef());
    }

    public LLVMValueRef classObjRef(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);

        LLVMValueRef[] classObjPtrs = classObjPtrs(rt).stream().toArray(LLVMValueRef[]::new);
        LLVMValueRef classObjPtrsArr = v.utils.buildConstArray(classIdVarPtrTypeRef(), classObjPtrs);
        LLVMValueRef numSupertypes = LLVMConstInt(LLVMInt32TypeInContext(v.context), countSupertypes(rt), /*sign-extend*/ 0);
        LLVMValueRef classObjStruct = v.utils.buildConstStruct(numSupertypes, classObjPtrsArr);

        LLVMValueRef global = v.utils.getGlobal(v.mod, v.mangler.classObjName(rt), LLVMTypeOf(classObjStruct));
        LLVMSetExternallyInitialized(global, 0);
        LLVMSetInitializer(global, classObjStruct);
        if (v.isInterface(rt)) {
            LLVMSetLinkage(global, LLVMLinkOnceODRLinkage);
        }

        return global;
    }

    /** Counts the supertypes for this reference type, including itself. */
    public int countSupertypes(ReferenceType rt) {
        return classObjPtrs(rt).size();
    }

    public List<LLVMValueRef> classObjPtrs(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);
        Set<LLVMValueRef> res = new LinkedHashSet<>();
        res.add(classIdVarRef(rt));
        if (rt.superType() != null)
            res.addAll(classObjPtrs(rt.superType().toReference()));
        for (ReferenceType it : rt.interfaces())
            res.addAll(classObjPtrs(it));
        return res.stream().collect(Collectors.toList());
    }

    public LLVMTypeRef classObjArrTypeRef(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);
        return LLVMArrayType(classIdVarPtrTypeRef(), countSupertypes(rt));
    }

    public LLVMTypeRef classObjTypeRef(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);
        return v.utils.structType(LLVMInt32TypeInContext(v.context), classObjArrTypeRef(rt));
    }
}
