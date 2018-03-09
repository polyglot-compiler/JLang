package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Call;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.types.*;
import polyglot.util.Copy;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.types.SubstMethodInstance;
import polyllvm.util.CollectUtils;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.LLVMTranslator.DispatchInfo;

import java.lang.Override;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Indicates whether this call can be dispatched directly, without using a dispatch table.
     * This is set to true to optimize calls when possible, and also to ensure that
     * direct calls remain direct after desugar transformations (e.g., qualified super
     * can be desugared to field accesses, so the fact that the call is direct is otherwise lost).
     */
    private boolean direct = false;

    @SuppressWarnings("WeakerAccess")
    public Call determineIfDirect(Call c) {
        PolyLLVMCallExt ext = (PolyLLVMCallExt) PolyLLVMExt.ext(c);
        if (ext.direct) return c; // Should always remain direct once set.

        boolean direct = false;

        // Calls through super are direct.
        if (c.target() instanceof Special)
            if (((Special) c.target()).kind().equals(Special.SUPER))
                direct = true;

        // Calls to private or final methods are direct.
        Flags methodFlags = c.methodInstance().flags();
        if (methodFlags.isPrivate() || methodFlags.isFinal())
            direct = true;

        // Calls to methods of a final class are direct.
        ReferenceType container = c.methodInstance().container();
        if (container.isClass() && container.toClass().flags().isFinal())
            direct = true;

        // Copy and return.
        if (!direct) return c;
        if (c == node) {
            c = Copy.Util.copy(c);
            ext = (PolyLLVMCallExt) PolyLLVMExt.ext(c);
        }
        ext.direct = true;
        return c;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Call c = (Call) super.typeCheck(tc);
        return determineIfDirect(c);
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Call n = node();
        MethodInstance mi = n.methodInstance();

        if (mi.flags().isStatic()) {
            translateStaticCall(v);
        } else if (direct) {
            translateDirectNonStaticCall(v);
        } else {
            translateNonStaticCall(v);
        }

        return super.leaveTranslateLLVM(v);
    }

    @Override
    public Call node() {
        return (Call) super.node();
    }

    private void translateNonStaticCall(LLVMTranslator v) {
        Call n = node();
        ReferenceType recvTy = n.target().type().toReference();
        SubstMethodInstance substM = (SubstMethodInstance) n.methodInstance();
        JL5MethodInstance origM = substM.base();
        LLVMValueRef x_recv = v.getTranslation(n.target());
        List<LLVMValueRef> x_args = n.arguments().stream()
                .<LLVMValueRef> map(v::getTranslation)
                .collect(Collectors.toList());
        LLVMValueRef[] ll_args = CollectUtils.toArray(x_recv,
                x_args, LLVMValueRef.class);
        LLVMValueRef func_ptr;

        DispatchInfo dispInfo = v.dispatchInfo(recvTy, origM);
        if (dispInfo.isClassDisp()) { // dispatching a class method
            LLVMValueRef cdv_ptr_ptr = v.obj.buildDispatchVectorElementPtr(x_recv, recvTy);
            LLVMValueRef cdv_ptr = LLVMBuildLoad(v.builder, cdv_ptr_ptr,
                    "cdv_ptr");
            LLVMValueRef func_ptr_ptr = v.utils.buildStructGEP(cdv_ptr, 0,
                    dispInfo.methodIndex());
            func_ptr = LLVMBuildLoad(v.builder, func_ptr_ptr,
                    "load_method_ptr");
            // Bitcast the function so that the formal types are the types that
            // the arguments were cast to by DesugarImplicitConversions. It is
            // needed due to potential mismatch between the types caused by
            // erasure.
            LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(recvTy,
                    substM.returnType(), substM.formalTypes());
            func_ptr = LLVM.LLVMBuildBitCast(
                    v.builder, func_ptr, v.utils.ptrTypeRef(func_ty_cast), "cast");

        } else { // dispatching an interface method
            ClassType intf = dispInfo.intfErasure();
            LLVMValueRef intf_id_global = v.classObjs.toTypeIdentity(intf);
            LLVMValueRef obj_bitcast = LLVMBuildBitCast(v.builder, x_recv,
                    v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)),
                    "cast_for_interface_call");
            int hash = v.utils.intfHash(intf);
            LLVMValueRef intf_id_hash_const = LLVMConstInt(
                    LLVMInt32TypeInContext(v.context), hash,
                    /* sign-extend */ 0);
            LLVMTypeRef get_intf_method_func_ty = v.utils.functionType(
                    v.utils.llvmBytePtr(), // void* return type
                    v.utils.llvmBytePtr(), // jobject*
                    LLVMInt32TypeInContext(v.context), // int
                    v.utils.llvmBytePtr(), // void*
                    LLVMInt32TypeInContext(v.context) // int
            );
            LLVMValueRef get_intf_method_func = v.utils.getFunction(
                    "__getInterfaceMethod", get_intf_method_func_ty);
            LLVMValueRef offset_local = LLVMConstInt(
                    LLVMInt32TypeInContext(v.context), dispInfo.methodIndex(),
                    /* sign-extend */ 0);
            LLVMValueRef intf_method_local = v.utils.buildFunCall(
                    get_intf_method_func, // ptr to method code
                    obj_bitcast, // the object
                    intf_id_hash_const, // id hash code
                    intf_id_global, // id
                    offset_local // method index
            );
            // Bitcast the function so that the formal types are the types that
            // the arguments were cast to by DesugarImplicitConversions. It is
            // needed due to potential mismatch between the types caused by
            // erasure.
            LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(intf,
                    substM.returnType(), substM.formalTypes());
            func_ptr = LLVM.LLVMBuildBitCast(
                    v.builder, intf_method_local, v.utils.ptrTypeRef(func_ty_cast), "cast");
        }

        LLVMValueRef val;
        if (substM.returnType().isVoid()) {
            val = v.utils.buildProcCall(func_ptr, ll_args);
        } else {
            val = v.utils.buildFunCall(func_ptr, ll_args);
        }
        v.addTranslation(n, val);
    }

    private void translateStaticCall(LLVMTranslator v) {
        Call n = node();
        MethodInstance substM = n.methodInstance();

        LLVMValueRef[] x_args = new LLVMValueRef[n.arguments().size()];
        for (int i = 0; i < x_args.length; ++i)
            x_args[i] = v.getTranslation(n.arguments().get(i));

        String func_name = v.mangler.mangleProcName(substM);
        LLVMTypeRef func_type = v.utils.toLLFuncTy(v.utils.retErasureLL(substM),
                v.utils.formalsErasureLL(substM));
        LLVMValueRef func_ptr = v.utils.getFunction(func_name,
                func_type);
        // Bitcast the function so that the formal types are the types that the
        // arguments were cast to by DesugarImplicitConversions. It is needed due
        // to potential mismatch between the types caused by erasure.
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(substM.returnType(),
                substM.formalTypes());
        func_ptr = LLVM.LLVMBuildBitCast(
                v.builder, func_ptr, v.utils.ptrTypeRef(func_ty_cast), "cast");

        LLVMValueRef val;
        if (substM.returnType().isVoid()) {
            val = v.utils.buildProcCall(func_ptr, x_args);
        } else {
            val = v.utils.buildFunCall(func_ptr, x_args);
        }
        v.addTranslation(n, val);
    }

    private void translateDirectNonStaticCall(LLVMTranslator v) {
        Call n = node();
        MethodInstance substM = n.methodInstance();

        String func_name = v.mangler.mangleProcName(substM);

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(
                n.target().type().toReference(), v.utils.retErasureLL(substM),
                v.utils.formalsErasureLL(substM));

        LLVMValueRef x_recv = v.getTranslation(n.target());
        LLVMValueRef[] x_args = Stream
                .concat(Stream.of(x_recv),
                        n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);

        LLVMValueRef func_ptr = v.utils.getFunction(func_name, func_ty);
        // Bitcast the function so that the formal types are the types that the
        // arguments were cast to by DesugarImplicitConversions. It is needed due
        // to potential mismatch between the types caused by erasure.
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(
                n.target().type().toReference(), substM.returnType(),
                substM.formalTypes());
        func_ptr = LLVM.LLVMBuildBitCast(
                v.builder, func_ptr, v.utils.ptrTypeRef(func_ty_cast), "cast");

        if (substM.returnType().isVoid()) {
            v.addTranslation(n, v.utils.buildProcCall(func_ptr, x_args));
        } else {
            v.addTranslation(n, v.utils.buildFunCall(func_ptr, x_args));
        }
    }
}
