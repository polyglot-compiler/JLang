//Copyright (C) 2018 Cornell University

package jlang.structures;

import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import polyglot.types.ArrayType;
import polyglot.types.FieldInstance;
import polyglot.types.ReferenceType;

/**
 * Defines the layout of Java instance objects in terms of LLVM IR.
 * This includes array objects.
 *
 * Translations wanting to access specific parts of an object
 * at runtime (e.g., a field) should go through this interface.
 */
public interface ObjectStruct {

    /**
     * Returns a named LLVM struct type representing the object layout
     * of a given class. This type may be opaque initially, but will be filled
     * in if a translation tries to access any of its components through the
     * other methods in this interface.
     */
    LLVMTypeRef structTypeRef(ReferenceType rt);

    /** Returns the size (in bytes) of the given class. */
    LLVMValueRef sizeOf(ReferenceType rt);

    /** Returns the size (in bytes) of the given class. */
    int sizeOfObj(ReferenceType rt);

    /** Returns a pointer to the dispatch dispatch vector. */
    LLVMValueRef buildDispatchVectorElementPtr(LLVMValueRef instance, ReferenceType rt);

    /** Returns a pointer to the specified field. */
    LLVMValueRef buildFieldElementPtr(LLVMValueRef instance, FieldInstance fi);

    /**
     * Returns a pointer to the base element of an array.
     * Assumes that the instance given is a Java array.
     */
    LLVMValueRef buildArrayBaseElementPtr(LLVMValueRef instance, ArrayType elemType);
}
