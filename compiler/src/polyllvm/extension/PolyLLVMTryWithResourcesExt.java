package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMTryWithResourcesExt extends PolyLLVMTryExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("Try-with-resources is unimplemented");
    }
}
