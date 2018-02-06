package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Call;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.types.SubstMethodInstance;
import polyllvm.util.CollectUtils;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.LLVMTranslator.DispatchInfo;

import java.lang.Override;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Call n = node();
        MethodInstance mi = n.methodInstance();

        if (n.target() instanceof Special
                && ((Special) n.target()).kind().equals(Special.SUPER)) {
            translateSuperCall(v);
        } else if (mi.flags().isStatic()) {
            translateStaticCall(v);
        } else if (mi.flags().isPrivate() || mi.flags().isFinal()) {
            translateFinalMethodCall(v);
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
                .<LLVMValueRef> map(e -> v.getTranslation(e))
                .collect(Collectors.toList());
        LLVMValueRef[] ll_args = CollectUtils.<LLVMValueRef> toArray(x_recv,
                x_args, LLVMValueRef.class);
        LLVMValueRef func_ptr;

        DispatchInfo dispInfo = v.dispatchInfo(recvTy, origM);
        if (dispInfo.isClassDisp()) { // dispatching a class method
            // Calling toLL ensures the LLVM types of the object and its CDV are
            // not opaque before the following GEPs occur
            v.utils.toLL(recvTy);
            LLVMValueRef cdv_ptr_ptr = v.utils.buildStructGEP(x_recv, 0,
                    Constants.DISPATCH_VECTOR_OFFSET);
            LLVMValueRef cdv_ptr = LLVMBuildLoad(v.builder, cdv_ptr_ptr,
                    "cdv_ptr");
            LLVMValueRef func_ptr_ptr = v.utils.buildStructGEP(cdv_ptr, 0,
                    dispInfo.methodIndex());
            func_ptr = LLVMBuildLoad(v.builder, func_ptr_ptr,
                    "load_method_ptr");
            // Bitcast the function so that the formal types are the types that
            // the arguments were cast to by MakeCastsExplicitVisitor. It is
            // needed due to potential mismatch between the types caused by
            // erasure.
            LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(recvTy,
                    substM.returnType(), substM.formalTypes());
            func_ptr = v.utils.bitcastFunc(func_ptr, func_ty_cast);

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
            LLVMValueRef get_intf_method_func = v.utils.getFunction(v.mod,
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
            // the arguments were cast to by MakeCastsExplicitVisitor. It is
            // needed due to potential mismatch between the types caused by
            // erasure.
            LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(intf,
                    substM.returnType(), substM.formalTypes());
            func_ptr = v.utils.bitcastFunc(intf_method_local, func_ty_cast);
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

        String func_name = v.mangler.mangleProcedureName(substM);
        LLVMTypeRef func_type = v.utils.toLLFuncTy(v.utils.retErasureLL(substM),
                v.utils.formalsErasureLL(substM));
        LLVMValueRef func_ptr = v.utils.getFunction(v.mod, func_name,
                func_type);
        // Bitcast the function so that the formal types are the types that the
        // arguments were cast to by MakeCastsExplicitVisitor. It is needed due
        // to potential mismatch between the types caused by erasure.
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(substM.returnType(),
                substM.formalTypes());
        func_ptr = v.utils.bitcastFunc(func_ptr, func_ty_cast);

        v.debugInfo.emitLocation(n);

        LLVMValueRef val;
        if (substM.returnType().isVoid()) {
            val = v.utils.buildProcCall(func_ptr, x_args);
        } else {
            val = v.utils.buildFunCall(func_ptr, x_args);
        }
        v.addTranslation(n, val);
    }

    private void translateFinalMethodCall(LLVMTranslator v) {
        Call n = node();
        MethodInstance substM = n.methodInstance();

        String func_name = v.mangler.mangleProcedureName(substM);

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(
                n.target().type().toReference(), v.utils.retErasureLL(substM),
                v.utils.formalsErasureLL(substM));

        LLVMValueRef x_recv = v.getTranslation(n.target());
        LLVMValueRef[] x_args = Stream
                .concat(Stream.of(x_recv),
                        n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);

        LLVMValueRef func_ptr = v.utils.getFunction(v.mod, func_name, func_ty);
        // Bitcast the function so that the formal types are the types that the
        // arguments were cast to by MakeCastsExplicitVisitor. It is needed due
        // to potential mismatch between the types caused by erasure.
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(
                n.target().type().toReference(), substM.returnType(),
                substM.formalTypes());
        func_ptr = v.utils.bitcastFunc(func_ptr, func_ty_cast);

        v.debugInfo.emitLocation(n);
        if (substM.returnType().isVoid()) {
            v.addTranslation(n, v.utils.buildProcCall(func_ptr, x_args));
        } else {
            v.addTranslation(n, v.utils.buildFunCall(func_ptr, x_args));
        }
    }

    private void translateSuperCall(LLVMTranslator v) {
        Call n = node();
        MethodInstance substM = n.methodInstance();

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(substM.container(),
                v.utils.retErasureLL(substM), v.utils.formalsErasureLL(substM));
        LLVMValueRef func_ptr = v.utils.getFunction(v.mod,
                v.mangler.mangleProcedureName(substM), func_ty);

        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(
                v.getCurrentClass().type(), substM.returnType(),
                substM.formalTypes());
        func_ptr = v.utils.bitcastFunc(func_ptr, func_ty_cast);

        LLVMValueRef x_this = LLVMGetParam(v.currFn(), 0);
        LLVMValueRef[] x_args = Stream
                .concat(Stream.of(x_this),
                        n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);

        if (substM.returnType().isVoid()) {
            v.addTranslation(n, v.utils.buildProcCall(func_ptr, x_args));
        } else {
            v.addTranslation(n, v.utils.buildFunCall(func_ptr, x_args));
        }
    }

}
