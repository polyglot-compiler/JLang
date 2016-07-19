package polyllvm.ast.PseudoLLVM.LLVMTypes;

public interface LLVMFunctionType extends LLVMTypeNode {

    LLVMFunctionType prependFormalTypeNode(LLVMTypeNode tn);

}
