package polyllvm.ast.PseudoLLVM.LLVMTypes;

public interface LLVMPointerType extends LLVMTypeNode {

    /**
     * Return the type which this pointer points to.
     */
    LLVMTypeNode dereferenceType();
}
