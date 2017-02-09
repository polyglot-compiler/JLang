package polyllvm.extension;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        // Add a malloc declaration to the current module.
        LLVMTypeRef retType = LLVMPointerType(LLVMInt8Type(), PolyLLVMConstants.LLVM_ADDR_SPACE);
        LLVMTypeRef argType = PolyLLVMTypeUtils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = PolyLLVMTypeUtils.llvmFunctionType(retType, argType);
        LLVMAddFunction(v.mod, "malloc", funcType);
        return v;
    }
}
