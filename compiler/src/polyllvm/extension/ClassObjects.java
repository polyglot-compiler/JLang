package polyllvm.extension;

import polyglot.types.ReferenceType;
import polyllvm.visit.LLVMTranslator;

import java.util.*;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Creates LLVM IR for class objects, which are used, e.g., to implement
 * instanceof.
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

    /**
     * Obtains the LLVM representation of the identity of Java class/interface
     * type {@code rt}. The identity is efficiently represented by a pointer to
     * a global variable allocated per class/interface.
     *
     * @param rt
     * @param extern
     *            indicates whether to initialize the global variable
     */
    public LLVMValueRef toTypeIdentity(ReferenceType rt, boolean extern) {
        LLVMValueRef global = v.utils.getGlobal(v.mod,
                v.mangler.typeIdentityId(v.utils.erasureLL(rt)),
                classIdVarTypeRef());
        if (!extern) {
            LLVMSetInitializer(global, LLVMConstInt(
                    LLVMInt8TypeInContext(v.context), 0, /* sign-extend */ 0));
        }
        return global;
    }

    /**
     * See {@link #toTypeIdentity(ReferenceType, boolean)} ({@code extern} is
     * always true).
     */
    public LLVMValueRef toTypeIdentity(ReferenceType rt) {
        return toTypeIdentity(rt, true);
    }

    public LLVMTypeRef classObjTypeRef(ReferenceType rt) {
        LLVMValueRef[] classObjPtrs = classObjPtrs(rt);
        return v.utils.structType(LLVMInt32TypeInContext(v.context),
                LLVMArrayType(classIdVarPtrTypeRef(), classObjPtrs.length));
    }

    public LLVMValueRef classObjRef(ReferenceType rt) {
        LLVMValueRef[] classObjPtrs = classObjPtrs(rt);
        LLVMValueRef classObjPtrsArr = v.utils
                .buildConstArray(classIdVarPtrTypeRef(), classObjPtrs);
        LLVMValueRef numSupertypes = LLVMConstInt(
                LLVMInt32TypeInContext(v.context), classObjPtrs.length,
                /* sign-extend */ 0);
        LLVMValueRef classObjStruct = v.utils.buildConstStruct(numSupertypes,
                classObjPtrsArr);

        LLVMValueRef global = v.utils.getGlobal(v.mod,
                v.mangler.classObjName(rt), LLVMTypeOf(classObjStruct));
        LLVMSetExternallyInitialized(global, 0);
        LLVMSetInitializer(global, classObjStruct);
        LLVMSetLinkage(global, LLVMLinkOnceODRLinkage);

        return global;
    }

    private LLVMValueRef[] classObjPtrs(ReferenceType rt) {
        // suptypes will contain the erasure of all supertypes
        Set<ReferenceType> suptypes = new LinkedHashSet<>();
        Deque<ReferenceType> toVisit = new LinkedList<>();
        toVisit.add(rt);
        while (!toVisit.isEmpty()) {
            ReferenceType t = toVisit.remove();
            suptypes.add(v.utils.erasureLL(t));
            if (t.superType() != null)
                toVisit.add(t.superType().toReference());
            for (ReferenceType it : t.interfaces())
                toVisit.add(it);
        }
        return suptypes.stream().map(t -> toTypeIdentity(t))
                .toArray(LLVMValueRef[]::new);
    }

}
