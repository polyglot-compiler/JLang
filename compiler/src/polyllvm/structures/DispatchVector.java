package polyllvm.structures;

import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;

/**
 * Defines the layout of a dispatch vector in terms of LLVM IR.
 * Translations wishing to access elements in a dispatch vector at
 * runtime should go through this interface.
 */
public interface DispatchVector {

    /** Returns an LLVM type representing the dispatch vector layout. */
    LLVMTypeRef structTypeRef(ReferenceType rt);

    /** Initializes the dispatch vector for a specific concrete class. */
    void initializeDispatchVectorFor(ReferenceType rt);

    /** Returns a pointer to the dispatch vector for a specific concrete class. */
    LLVMValueRef getDispatchVectorFor(ReferenceType rt);

    /** Returns a pointer to the specified method in a dispatch vector. */
    LLVMValueRef buildFuncElementPtr(LLVMValueRef dvPtr, ReferenceType rt, MethodInstance pi);
}
