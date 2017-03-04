package polyllvm.extension;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;
import java.util.function.Supplier;
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
            buidEntryPoint(v, entryPoints.iterator().next());
        }

        // Build ctor functions, if any.
        buildCtors(v);

        return super.translatePseudoLLVM(v);
    }

    /**
     * Build a trampoline between the LLVM entry point and the Java entry point.
     */
    private static void buidEntryPoint(PseudoLLVMTranslator v, LLVMValueRef javaEntryPoint) {
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

    /**
     * Build ctor functions using the ctor suppliers added to the visitor during translation.
     */
    private static void buildCtors(PseudoLLVMTranslator v) {
        List<Supplier<LLVMValueRef>> ctors = v.getCtors();
        if (ctors.isEmpty())
            return;

        // Create the ctor global array as specified in the LLVM Language Reference Manual.
        LLVMTypeRef funcType = LLVMUtils.functionType(LLVMVoidType());
        LLVMTypeRef funcPtrType = LLVMUtils.ptrTypeRef(funcType);
        LLVMTypeRef voidPtr = LLVMUtils.ptrTypeRef(LLVMInt8Type());
        LLVMTypeRef structType = LLVMUtils.structType(LLVMInt32Type(), funcPtrType, voidPtr);
        LLVMTypeRef ctorVarType = LLVMArrayType(structType, /*size*/ ctors.size());
        String ctorVarName = "llvm.global_ctors";
        LLVMValueRef ctorGlobal = LLVMUtils.getGlobal(v.mod, ctorVarName, ctorVarType);
        LLVMSetLinkage(ctorGlobal, LLVMAppendingLinkage);

        // For each ctor function, create a struct containing a priority, a pointer to the
        // ctor function, and a pointer to associated data if applicable.
        LLVMValueRef[] structs = new LLVMValueRef[ctors.size()];
        int counter = 0;
        for (Supplier<LLVMValueRef> ctor : ctors) {
            LLVMValueRef func = LLVMUtils.getFunction(v.mod, "ctor" + counter, funcType);
            LLVMSetLinkage(func, LLVMPrivateLinkage);
            LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(
                    v.debugInfo.diBuilder, new PointerPointer<>(), /*length*/ 0);
            LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(
                    v.debugInfo.diBuilder, v.debugInfo.createFile(), typeArray);
            v.debugInfo.funcDebugInfo(v, 0, ctorVarName, ctorVarName, funcDiType, func);

            LLVMBasicBlockRef body = LLVMAppendBasicBlock(func, "body");
            LLVMPositionBuilderAtEnd(v.builder, body);

            // We use `counter` as the ctor priority to help ensure that static initializers
            // are executed in textual order, per the JLS.
            LLVMValueRef priority = LLVMConstInt(LLVMInt32Type(), counter, /*sign-extend*/ 0);
            LLVMValueRef data = ctor.get(); // Calls supplier lambda to build ctor body.
            if (data == null)
                data = LLVMConstNull(voidPtr);
            LLVMValueRef castData = LLVMConstBitCast(data, voidPtr);
            structs[counter] = LLVMUtils.buildConstStruct(priority, func, castData);

            LLVMBuildRetVoid(v.builder);
            v.debugInfo.popScope();

            ++counter;
        }

        LLVMValueRef arr = LLVMUtils.buildConstArray(structType, structs);
        LLVMSetInitializer(ctorGlobal, arr);
    }
}
