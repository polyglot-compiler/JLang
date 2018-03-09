package polyllvm.structures;

import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;
import polyglot.types.ClassType;
import polyglot.types.ProcedureInstance;

/**
 * Defines the layout of a dispatch vector in terms of LLVM IR.
 * Translations wishing to access elements in a dispatch vector at
 * runtime should go through this interface.
 */
public interface DispatchVector {

    /** Return an LLVM type representing the dispatch vector layout. */
    LLVMTypeRef buildTypeRef(ClassType ct);

    /** Returns a pointer to the specified method in a dispatch vector. */
    LLVMValueRef buildFuncElementPtr(LLVMValueRef dv, ProcedureInstance pi);
}
