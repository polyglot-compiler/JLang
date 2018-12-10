//Copyright (C) 2018 Cornell University

package jlang.extension;

import org.bytedeco.javacpp.LLVM;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.*;
import polyglot.util.SerialVersionUID;

import java.lang.Override;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static jlang.util.Constants.GET_NATIVE_FUNC;
import static jlang.util.Constants.JNI_ENV_VAR_NAME;
import static org.bytedeco.javacpp.LLVM.*;

public class JLangProcedureDeclExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();
        ClassType ct = pi.container().toClass();

        // Build JNI trampoline to allow this method to be called from native code.
        buildJniTrampoline(v);

        if (pi.flags().isAbstract() || ct.flags().isInterface())
            return super.overrideTranslateLLVM(parent, v); // Ignore abstract/interface methods.
        assert pi.container().isClass();

        String funcName = v.mangler.proc(pi);
        String debugName = ct.fullName() + "#" + pi.signature();

        Type retType = v.utils.erasedReturnType(pi);
        List<Type> argTypes = v.utils.erasedImplicitFormalTypes(pi);

        Runnable buildBody = () -> {

            // Initialize formals.
            if (pi.flags().isNative()) {
                // Native method stubs will simply forward their arguments,
                // so there's no need to initialize their formals here.
            }
            else {
                for (int i = 0; i < n.formals().size(); ++i) {
                    Formal formal = n.formals().get(i);
                    LocalInstance li = formal.localInstance().orig();
                    LLVMTypeRef typeRef = v.utils.toLL(formal.declType());
                    LLVMValueRef alloca = v.utils.buildAlloca(formal.name(), typeRef);

                    v.addTranslation(li, alloca);
                    v.debugInfo.createParamVariable(v, formal, i, alloca);

                    int idx = i + (pi.flags().isStatic() ? 0 : 1);
                    LLVMValueRef param = LLVMGetParam(v.currFn(), idx);
                    LLVMBuildStore(v.builder, param, alloca);
                }
            }

            // If static method or constructor, make sure the container class has been initialized.
            // See JLS 7, section 12.4.1.
            if (pi.flags().isStatic() || pi instanceof ConstructorInstance) {
                v.utils.buildClassLoadCheck(ct);
            }

            // Register as entry point if applicable.
            boolean isEntryPoint = n.name().equals("main")
                    && n.flags().isPublic()
                    && n.flags().isStatic()
                    && n.formals().size() == 1
                    && n.formals().iterator().next().declType().equals(v.ts.arrayOf(v.ts.String()));
            if (isEntryPoint) {
                String className = n.procedureInstance().container().toClass().fullName();
                v.addEntryPoint(v.currFn(), className);

                // For bootstrapping reasons, initialize the java.lang.Class class first.
                v.utils.buildClassLoadCheck(v.ts.Class());

                // Initialize the java.lang.String class at each entry point to avoid
                // the need for class loading before string literals.
                v.utils.buildClassLoadCheck(v.ts.String());
            }

            if (n.body() != null) {
                // Recurse to children.
                lang().visitChildren(n, v);
            }
            else {
                assert n.flags().isNative();
                // Build trampoline to call the "real" native method.

                // Call into the runtime to retrieve the native function pointer.
                // (Note that we could cache the function pointer if performance is an issue.)
                // The arguments here must precisely match the signature of
                // the function defined in the runtime.
                LLVMValueRef clazz = v.utils.loadClassObject(ct);
                LLVMValueRef[] runtimeCallArgs = {
                        clazz,
                        v.utils.buildGlobalCStr(n.name()),
                        v.utils.buildGlobalCStr(v.mangler.jniUnescapedSignature(pi)),
                        v.utils.buildGlobalCStr(v.mangler.shortNativeSymbol(pi)),
                        v.utils.buildGlobalCStr(v.mangler.longNativeSymbol(pi)),
                };
                LLVMTypeRef[] runtimeCallArgTypes = Stream.of(runtimeCallArgs)
                        .map(LLVM::LLVMTypeOf)
                        .toArray(LLVMTypeRef[]::new);
                LLVMTypeRef runtimeCallType = v.utils.functionType(
                        v.utils.i8Ptr(), runtimeCallArgTypes);
                LLVMValueRef runtimeFunc = v.utils.getFunction(GET_NATIVE_FUNC, runtimeCallType);
                LLVMValueRef rawFuncPtr = v.utils.buildFunCall(runtimeFunc, runtimeCallArgs);

                // Get JNIEnv and forward all args to the native method call.
                List<LLVMValueRef> args = new ArrayList<>();
                args.add(v.utils.getGlobal(JNI_ENV_VAR_NAME, v.utils.jniEnvType()));
                if (pi.flags().isStatic())
                    args.add(clazz);
                for (int i = 0, e = LLVMCountParams(v.currFn()); i < e; ++i)
                    args.add(LLVMGetParam(v.currFn(), i));
                LLVMValueRef[] argArr = args.toArray(new LLVMValueRef[args.size()]);

                LLVMTypeRef funcType = v.utils.getNativeFunctionType(pi);
                LLVMValueRef funcPtr = LLVMBuildBitCast(
                        v.builder, rawFuncPtr, v.utils.ptrTypeRef(funcType), "native.func.ptr");

                if (retType.isVoid()) {
                     v.utils.buildProcCall(funcPtr, argArr);
                     LLVMBuildRetVoid(v.builder);
                }
                else {
                    LLVMValueRef result = v.utils.buildFunCall(funcPtr, argArr);
                    LLVMBuildRet(v.builder, result);
                }
            }
        };

        v.utils.buildFunc(n.position(), funcName, debugName, retType, argTypes, buildBody);

        return n;
    }

    /**
     * Builds the trampoline necessary to be able to call this function from native code.
     *
     * Calling Java methods from native code is not as simple as finding the right function
     * pointer, because we also need to use the right calling conventions. One option would be
     * to use something like libffi to move arguments into the correct registers at runtime.
     * We take a different approach: we generate a trampoline for each Java method, which
     * takes in a function pointer, casts it to the correct type for a given method, and then
     * calls through the pointer with arguments taken from an argument array.
     *
     * We reduce code bloat by using linkonce_odr linkage (and ensuring that trampolines
     * with the same calling conventions have the same name).
     *
     * The function definition looks like this:
     *     jtype trampoline(void* fnPtr, jvalue* args);
     * Where jtype is the return type of this method, and jvalue is some
     * type that is large enough to hold any Java value (e.g., i64).
     */
    protected void buildJniTrampoline(LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();

        String name = v.mangler.procJniTrampoline(pi);

        LLVMValueRef preexisting = LLVMGetNamedFunction(v.mod, name);
        if (preexisting != null && LLVMIsDeclaration(preexisting) == 0)
            return; // We've already built this trampoline in this module.

        Function<Type, Type> mergeReferenceTypes = t -> t.isReference() ? v.ts.Object() : t;

        Type returnType = mergeReferenceTypes.apply(v.utils.erasedReturnType(pi));
        LLVMTypeRef returnTypeLL = v.utils.toLL(returnType);

        // Parameter debug types.
        LLVMMetadataRef fnPtrDebugType = LLVMDIBuilderCreateBasicType(
                v.debugInfo.diBuilder, "void*", 8 * v.utils.llvmPtrSize(), DW_ATE_address);
        LLVMMetadataRef argsDebugType = LLVMDIBuilderCreateBasicType(
                v.debugInfo.diBuilder, "jvalue*", 8 * v.utils.llvmPtrSize(), DW_ATE_address);
        List<LLVMMetadataRef> argDebugTypes = Arrays.asList(fnPtrDebugType, argsDebugType);

        // Parameter types.
        // These should stay consistent with LLVMUtils#toLLTrampoline.
        LLVMTypeRef fnPtrType = v.utils.i8Ptr();
        LLVMTypeRef argsType = v.utils.ptrTypeRef(v.utils.i64());
        List<LLVMTypeRef> argTypesLL = Arrays.asList(fnPtrType, argsType);

        Runnable buildBody = () -> {

            // Ensure this definition is merged with equivalent trampolines.
            LLVMSetLinkage(v.currFn(), LLVMLinkOnceODRLinkage);

            LLVMValueRef fnPtr = LLVMGetParam(v.currFn(), 0);
            LLVMValueRef argsPtr = LLVMGetParam(v.currFn(), 1);

            // Collect arguments.
            List<Type> forwardArgTypes = v.utils.erasedImplicitFormalTypes(pi);
            List<LLVMValueRef> forwardArgs = new ArrayList<>();
            for (int i = 0; i < forwardArgTypes.size(); ++i) {
                LLVMValueRef ptr = v.utils.buildGEP(argsPtr, i);
                Type forwardArgType = mergeReferenceTypes.apply(forwardArgTypes.get(i));
                LLVMTypeRef castType = v.utils.ptrTypeRef(v.utils.toLL(forwardArgType));
                LLVMValueRef cast = LLVMBuildBitCast(v.builder, ptr, castType, "cast.arg." + i);
                LLVMValueRef load = LLVMBuildLoad(v.builder, cast, "load.arg." + i);
                forwardArgs.add(load);
            }

            // Cast function pointer to the correct type.
            LLVMTypeRef[] castArgTypes = forwardArgTypes.stream()
                    .map(mergeReferenceTypes)
                    .map(v.utils::toLL)
                    .toArray(LLVMTypeRef[]::new);
            LLVMTypeRef fnCastType = v.utils.ptrTypeRef(
                    v.utils.functionType(returnTypeLL, castArgTypes));
            fnPtr = LLVMBuildBitCast(v.builder, fnPtr, fnCastType, "cast.func");

            LLVMValueRef[] forwardArgsArr = forwardArgs.toArray(new LLVMValueRef[0]);
            if (returnType.isVoid()) {
                v.utils.buildProcCall(fnPtr, forwardArgsArr);
                LLVMBuildRetVoid(v.builder);
            } else {
                LLVMValueRef res = v.utils.buildFunCall(fnPtr, forwardArgsArr);
                LLVMBuildRet(v.builder, res);
            }
        };

        v.utils.buildFunc(
                n.position(), name, /*debugName*/ name,
                returnTypeLL, argTypesLL, argDebugTypes,
                buildBody);
    }
}
