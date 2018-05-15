package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.ConstructorInstance;
import polyglot.types.LocalInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;
import static polyllvm.util.Constants.GET_NATIVE_FUNC;
import static polyllvm.util.Constants.JNI_ENV_VAR_NAME;

public class PolyLLVMProcedureDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private static boolean noImplementation(ProcedureInstance pi) {
        return pi.flags().isNative() || pi.flags().isAbstract();
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();

        if (pi.flags().isAbstract())
            return super.overrideTranslateLLVM(parent, v); // Ignore abstract methods.
        assert pi.container().isClass();

        String funcName = v.mangler.proc(pi);
        String debugName = pi.container().toClass().fullName() + "#" + pi.signature();

        Type retType = v.utils.erasedReturnType(pi);
        List<Type> argTypes = v.utils.erasedFormalTypes(pi);

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
                v.utils.buildClassLoadCheck(pi.container().toClass());
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

                // Initialize the java.lang.String class at each entry point to avoid
                // the need for class loading before string literals.
                v.utils.buildClassLoadCheck(v.ts.String());
            }

            if (n.body() != null) {
                // Recurse to children.
                lang().visitChildren(n, v);
            }
            else {
                // Build trampoline to call the "real" native method.

                // Call into the runtime to retrieve the native function pointer.
                // (Note that we could cache the function pointer if performance is an issue.)
                // The arguments here must precisely match the signature of
                // the function defined in the runtime.
                LLVMValueRef clazz = v.utils.loadClassObject(pi.container().toClass());
                LLVMValueRef[] runtimeCallArgs = {
                        clazz,
                        v.utils.buildGlobalCStr(n.name()),
                        v.utils.buildGlobalCStr(v.mangler.typeSignature(pi)),
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
}
