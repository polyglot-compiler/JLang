//Copyright (C) 2018 Cornell University

package jlang.extension;

import org.bytedeco.javacpp.LLVM.LLVMMetadataRef;
import org.bytedeco.javacpp.PointerPointer;

import jlang.JLangOptions;
import jlang.ast.JLangExt;
import jlang.util.Constants;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;
import polyglot.main.Options;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;

import java.lang.Override;
import java.util.Map;
import java.util.stream.Stream;

import static jlang.util.Constants.ENTRY_TRAMPOLINE;
import static org.bytedeco.javacpp.LLVM.*;

public class JLangSourceFileExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LLVMTranslator enterTranslateLLVM(LLVMTranslator v) {
        // Add a calloc declaration to the current module (declare i8* @GC_malloc(i64)).
        LLVMTypeRef retType = v.utils.ptrTypeRef(v.utils.i8());
        LLVMTypeRef sizeType = v.utils.llvmPtrSizedIntType();
        LLVMTypeRef funcType = v.utils.functionType(retType, sizeType);
        LLVMAddFunction(v.mod, Constants.CALLOC, funcType);

        return v;
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {

        // Call an entry point within the current module if possible.
        Map<String, LLVMValueRef> entryPoints = v.getEntryPoints();
        JLangOptions options = (JLangOptions) Options.global;
        String entryPointClass = options.entryPointClass;

        if (entryPointClass != null) {
            if (entryPoints.containsKey(entryPointClass)) {
                buildEntryPoint(v, entryPoints.get(entryPointClass));
            }
        }
        else {
            // Try to emit an entry point even if the user did not specify one.
            // If there are multiple entry points, this may result in duplicate
            // symbols during linking.
            for (String entry : entryPoints.keySet()) {
                buildEntryPoint(v, entryPoints.get(entry));
            }
        }

        // Build ctor functions, if any.
        buildCtors(v);

        return super.leaveTranslateLLVM(v);
    }

    /**
     * Build a trampoline between the LLVM entry point and the Java entry point.
     */
    private static void buildEntryPoint(LLVMTranslator v, LLVMValueRef javaEntryPoint) {
        TypeSystem ts = v.ts;
        LLVMTypeRef jniEnvT = v.utils.ptrTypeRef(v.utils.jniEnvType());
        LLVMTypeRef classObj = v.utils.toLL(v.ts.Class());
        LLVMTypeRef strArgsT = v.utils.toLL(ts.arrayOf(ts.String()));
        LLVMTypeRef voidT = LLVMVoidTypeInContext(v.context);
        LLVMTypeRef funcType = v.utils.functionType(voidT, jniEnvT, classObj, strArgsT);

        LLVMValueRef func = LLVMAddFunction(v.mod, ENTRY_TRAMPOLINE, funcType);
        v.pushFn(func);

        LLVMMetadataRef[] formals = Stream.of(ts.Object(), ts.Class(), ts.arrayOf(ts.String()))
                .map(v.debugInfo::debugType)
                .toArray(LLVMMetadataRef[]::new);
       PointerPointer<LLVMMetadataRef> typeArray = new PointerPointer<>(formals);
       LLVMDIBuilderGetOrCreateTypeArray(
                v.debugInfo.diBuilder, typeArray, formals.length);
        LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(
                v.debugInfo.diBuilder, v.debugInfo.debugFile, typeArray, formals.length, LLVMDIFlagZero);
        v.debugInfo.beginFuncDebugInfo(func, ENTRY_TRAMPOLINE, "Java_entry_point", funcDiType, 0);

        LLVMBasicBlockRef block = LLVMAppendBasicBlockInContext(v.context, func, "body");
        LLVMPositionBuilderAtEnd(v.builder, block);

        v.utils.buildProcCall(javaEntryPoint, LLVMGetParam(func, 2));
        LLVMBuildRetVoid(v.builder);
        v.debugInfo.popScope();

        v.popFn();
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
        LLVMTypeRef voidPtr = v.utils.i8Ptr();
        LLVMTypeRef structType = v.utils.structType(LLVMInt32TypeInContext(v.context), funcPtrType, voidPtr);
        LLVMTypeRef ctorVarType = LLVMArrayType(structType, ctors.length);
        String ctorVarName = Constants.CTOR_VAR_NAME;
        LLVMValueRef ctorGlobal = v.utils.getGlobal(ctorVarName, ctorVarType);
        LLVMSetLinkage(ctorGlobal, LLVMAppendingLinkage);

        LLVMValueRef arr = v.utils.buildConstArray(structType, ctors);
        LLVMSetInitializer(ctorGlobal, arr);
    }
}
