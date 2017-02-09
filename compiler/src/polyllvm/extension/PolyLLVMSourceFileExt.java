package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        // Add a malloc declaration to the current module.
        LLVMTypeRef retType = LLVMUtils.ptrTypeRef(LLVMInt8Type());
        LLVMTypeRef argType = LLVMUtils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = LLVMUtils.functionType(retType, argType);
        LLVMAddFunction(v.mod, Constants.MALLOC, funcType);
        return v;
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        // Call an entry point within the current module if possible.
        List<LLVMValueRef> entryPoints = v.getEntryPoints();
        if (!entryPoints.isEmpty()) {
            // Build a trampoline between the LLVM entry point and the Java entry point.
            LLVMValueRef javaEntryPoint = entryPoints.iterator().next();
            TypeSystem ts = v.typeSystem();
            LLVMTypeRef argType = LLVMUtils.typeRef(ts.arrayOf(ts.String()), v.mod);
            LLVMTypeRef funcType = LLVMUtils.functionType(LLVMVoidType(), argType);
            LLVMValueRef func = LLVMAddFunction(v.mod, Constants.ENTRY_TRAMPOLINE, funcType);
            LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "body");
            LLVMPositionBuilderAtEnd(v.builder, block);
            LLVMUtils.buildCall(v.builder, javaEntryPoint, LLVMGetFirstParam(func));
            LLVMBuildRetVoid(v.builder);
        }
        return super.translatePseudoLLVM(v);
    }
}
