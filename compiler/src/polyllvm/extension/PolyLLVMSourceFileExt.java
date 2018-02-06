package polyllvm.extension;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.Node;
import polyglot.main.Options;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.PolyLLVMOptions;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Map;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LLVMTranslator enterTranslateLLVM(LLVMTranslator v) {
        // Add a calloc declaration to the current module (declare i8* @GC_malloc(i64)).
        LLVMTypeRef retType = v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context));
        LLVMTypeRef sizeType = v.utils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = v.utils.functionType(retType, sizeType);
        LLVMAddFunction(v.mod, Constants.CALLOC, funcType);

        return v;
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {

        // Call an entry point within the current module if possible.
        Map<String, LLVMValueRef> entryPoints = v.getEntryPoints();
        String entryPointClass = ((PolyLLVMOptions) Options.global).entryPointClass;
        if (entryPointClass != null) {
            if (entryPoints.containsKey(entryPointClass)) {
                buildEntryPoint(v, entryPoints.get(entryPointClass));
            } else {
                //TODO: Should this be a different error?
                throw new InternalCompilerError("No entry point found for class " + entryPointClass);
            }
        } else if (!entryPoints.isEmpty()) {
            buildEntryPoint(v, entryPoints.values().iterator().next());
        }

        // Build ctor functions, if any.
        buildCtors(v);

        return super.leaveTranslateLLVM(v);
    }

    /**
     * Build a trampoline between the LLVM entry point and the Java entry point.
     */
    private static void buildEntryPoint(LLVMTranslator v, LLVMValueRef javaEntryPoint) {
        TypeSystem ts = v.typeSystem();
        LLVMTypeRef argType = v.utils.toLL(ts.arrayOf(ts.String()));
        LLVMTypeRef funcType = v.utils.functionType(LLVMVoidTypeInContext(v.context), argType);

        LLVMValueRef func = LLVMAddFunction(v.mod, Constants.ENTRY_TRAMPOLINE, funcType);

        LLVMMetadataRef[] formals = Stream.of(ts.arrayOf(ts.String())).map(v.debugInfo::debugType).toArray(LLVMMetadataRef[]::new);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(v.debugInfo.diBuilder, new PointerPointer<>(formals), formals.length);
        LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(v.debugInfo.diBuilder, v.debugInfo.createFile(), typeArray);
        v.debugInfo.funcDebugInfo(0, Constants.ENTRY_TRAMPOLINE, Constants.ENTRY_TRAMPOLINE, funcDiType, func);

        LLVMBasicBlockRef block = LLVMAppendBasicBlockInContext(v.context, func, "body");
        LLVMPositionBuilderAtEnd(v.builder, block);
        v.debugInfo.emitLocation();

        v.utils.buildProcCall(javaEntryPoint, LLVMGetFirstParam(func));
        LLVMBuildRetVoid(v.builder);
        v.debugInfo.popScope();
    }

    /**
     * Build ctor functions using the ctor suppliers added to the visitor during translation.
     */
    private static void buildCtors(LLVMTranslator v) {
        LLVMValueRef[] ctors = v.getCtors().toArray(new LLVMValueRef[0]);
        if (ctors.length == 0)
            return;

        // Create the ctor global array as specified in the LLVM Language Reference Manual.
        LLVMTypeRef funcType = v.utils.functionType(LLVMVoidTypeInContext(v.context));
        LLVMTypeRef funcPtrType = v.utils.ptrTypeRef(funcType);
        LLVMTypeRef voidPtr = v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context));
        LLVMTypeRef structType = v.utils.structType(LLVMInt32TypeInContext(v.context), funcPtrType, voidPtr);
        LLVMTypeRef ctorVarType = LLVMArrayType(structType, ctors.length);
        String ctorVarName = Constants.CTOR_VAR_NAME;
        LLVMValueRef ctorGlobal = v.utils.getGlobal(v.mod, ctorVarName, ctorVarType);
        LLVMSetLinkage(ctorGlobal, LLVMAppendingLinkage);

        LLVMValueRef arr = v.utils.buildConstArray(structType, ctors);
        LLVMSetInitializer(ctorGlobal, arr);
    }
}
