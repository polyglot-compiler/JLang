package polyllvm.extension;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.Node;
import polyglot.types.ProcedureInstance;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        // Add a calloc declaration to the current module (declare i8* @GC_malloc(i64)).
        LLVMTypeRef retType = LLVMUtils.ptrTypeRef(LLVMInt8Type());
        LLVMTypeRef sizeType = LLVMUtils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = LLVMUtils.functionType(retType, sizeType);
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

            LLVMMetadataRef[] formals = Stream.of(ts.arrayOf(ts.String())).map(v.debugInfo::debugType).toArray(LLVMMetadataRef[]::new);
            LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(v.debugInfo.diBuilder, new PointerPointer<>(formals), formals.length);
            LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(v.debugInfo.diBuilder, v.debugInfo.createFile(), typeArray);
            v.debugInfo.funcDebugInfo(v, 0, Constants.ENTRY_TRAMPOLINE, Constants.ENTRY_TRAMPOLINE, funcDiType, func);


            LLVMBasicBlockRef block = LLVMAppendBasicBlock(func, "body");
            LLVMPositionBuilderAtEnd(v.builder, block);

            v.debugInfo.emitLocation();

            LLVMUtils.buildProcedureCall(v, javaEntryPoint, LLVMGetFirstParam(func));
            LLVMBuildRetVoid(v.builder);

            v.debugInfo.popScope();
        }
        return super.translatePseudoLLVM(v);
    }
}
