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
        // Add a calloc declaration to the current module (declare i8* @calloc(i64, i64)).
        LLVMTypeRef retType = LLVMUtils.ptrTypeRef(LLVMInt8Type());
        LLVMTypeRef numType = LLVMUtils.llvmPtrSizedIntType();
        LLVMTypeRef sizeType = LLVMUtils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = LLVMUtils.functionType(retType, numType, sizeType);
        LLVMAddFunction(v.mod, Constants.CALLOC, funcType);
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
            LLVMTypeRef argType = LLVMUtils.typeRef(ts.arrayOf(ts.String()), v);
            LLVMTypeRef funcType = LLVMUtils.functionType(LLVMVoidType(), argType);
            LLVMValueRef func = LLVMAddFunction(v.mod, Constants.ENTRY_TRAMPOLINE, funcType);
            LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "body");
            LLVMPositionBuilderAtEnd(v.builder, block);
            LLVMUtils.buildProcedureCall(v.builder, javaEntryPoint, LLVMGetFirstParam(func));
            LLVMBuildRetVoid(v.builder);
        }
        return super.translatePseudoLLVM(v);
    }
}
