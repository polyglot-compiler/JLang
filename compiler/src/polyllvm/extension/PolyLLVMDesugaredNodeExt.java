package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMDesugaredNodeExt extends PolyLLVMExt {
    private static final String errorMsg =
            "This node should be desugared before translating to LLVM IR";

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        throw new InternalCompilerError(errorMsg + ": " + node().getClass());
    }
}
