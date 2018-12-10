//Copyright (C) 2018 Cornell University

package jlang.structures;

import polyglot.types.ReferenceType;

import java.util.*;

import jlang.visit.LLVMTranslator;

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
        return v.utils.i8();
    }

    public LLVMTypeRef classIdVarPtrTypeRef() {
        return v.utils.ptrTypeRef(classIdVarTypeRef());
    }

    /**
     * Obtains the LLVM representation of the identity of Java class/interface
     * type {@code rt}. The identity is efficiently represented by a pointer to
     * a global variable allocated per class/interface.
     *
     * @param extern indicates whether to initialize the global variable
     */
    public LLVMValueRef toTypeIdentity(ReferenceType rt, boolean extern) {
        // TODO: We could reuse the class object as the type identity.
        LLVMValueRef global = v.utils.getGlobal(
                v.mangler.typeIdentityId(v.utils.erasureLL(rt)),
                classIdVarTypeRef());
        if (!extern)
            LLVMSetInitializer(global, LLVMConstInt(v.utils.i8(), 0, /* sign-extend */ 0));
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
        LLVMValueRef classObjStruct = v.utils.buildConstStruct(numSupertypes, classObjPtrsArr);

        LLVMValueRef global = v.utils.getGlobal(
                v.mangler.typeInfo(rt), LLVMTypeOf(classObjStruct));
        LLVMSetExternallyInitialized(global, 0);
        LLVMSetInitializer(global, classObjStruct);
        LLVMSetLinkage(global, LLVMLinkOnceODRLinkage);

        return global;
    }

    private LLVMValueRef[] classObjPtrs(ReferenceType rt) {
        // supTypes will contain the erasure of all supertypes.
        Set<ReferenceType> supTypes = new LinkedHashSet<>();
        Deque<ReferenceType> toVisit = new LinkedList<>();
        toVisit.add(rt);

        while (!toVisit.isEmpty()) {
            ReferenceType t = toVisit.remove();
            supTypes.add(v.utils.erasureLL(t));
            if (t.superType() != null)
                toVisit.add(t.superType().toReference());
            toVisit.addAll(t.interfaces());
        }

        return supTypes.stream()
                .map(this::toTypeIdentity)
                .toArray(LLVMValueRef[]::new);
    }

}
