package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Call;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.types.*;
import polyglot.util.Copy;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.LLVMTranslator.DispatchInfo;

import java.lang.Override;

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

    /** Sets {@link this#direct} appropriately. */
    public Call determineIfDirect(Call c) {
        PolyLLVMCallExt ext = (PolyLLVMCallExt) PolyLLVMExt.ext(c);
        if (ext.direct) return c; // Should always remain direct once set.

        boolean direct = false;

        // Static, private, and final methods are direct.
        Flags methodFlags = c.methodInstance().flags();
        if (methodFlags.isStatic() || methodFlags.isPrivate() || methodFlags.isFinal())
            direct = true;

        // Calls through super are direct.
        if (c.target() instanceof Special)
            if (((Special) c.target()).kind().equals(Special.SUPER))
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
    protected LLVMValueRef buildFuncPtr(LLVMTranslator v, LLVMTypeRef funcType) {
        Call n = node();
        MethodInstance mi = n.methodInstance();

        if (direct) {
            // Direct (static, final, private, etc.) call.
            return super.buildFuncPtr(v, funcType);
        } else {
            ReferenceType recvTy = n.target().type().toReference();
            if (recvTy.isClass() && recvTy.toClass().flags().isInterface()) {
                // Interface method call.
                return buildInterfaceMethodPtr(v, mi, funcType);
            } else {
                // Class instance method call.
                return buildInstanceMethodPtr(v, mi);
            }
        }
    }

    protected LLVMValueRef buildInstanceMethodPtr(LLVMTranslator v, MethodInstance mi) {
        Call n = node();
        LLVMValueRef recv = v.getTranslation(n.target());
        ReferenceType recvTy = n.target().type().toReference();

        LLVMValueRef cdvPtrPtr = v.obj.buildDispatchVectorElementPtr(recv, recvTy);
        LLVMValueRef cdvPtr = LLVMBuildLoad(v.builder, cdvPtrPtr, "load.dv");
        LLVMValueRef funcPtrPtr = v.dv.buildFuncElementPtr(cdvPtr, recvTy, mi);

        return LLVMBuildLoad(v.builder, funcPtrPtr, "load.dv.method");
    }

    protected LLVMValueRef buildInterfaceMethodPtr(
            LLVMTranslator v, MethodInstance mi, LLVMTypeRef funcType) {
        Call n = node();
        LLVMValueRef recv = v.getTranslation(n.target());
        ReferenceType recvTy = n.target().type().toReference();
        DispatchInfo dispInfo = v.dispatchInfo(recvTy, mi);

        ClassType intf = dispInfo.intfErasure();
        LLVMValueRef intf_id_global = v.classObjs.toTypeIdentity(intf);
        LLVMValueRef obj_bitcast = LLVMBuildBitCast(v.builder, recv, v.utils.i8Ptr(), "cast.obj");
        int hash = v.utils.intfHash(intf);
        LLVMValueRef intf_id_hash_const = LLVMConstInt(
                LLVMInt32TypeInContext(v.context), hash,
                /* sign-extend */ 0);
        LLVMTypeRef get_intf_method_func_ty = v.utils.functionType(
                v.utils.i8Ptr(), // void* return type
                v.utils.i8Ptr(), // jobject*
                LLVMInt32TypeInContext(v.context), // int
                v.utils.i8Ptr(), // void*
                LLVMInt32TypeInContext(v.context) // int
        );
        LLVMValueRef get_intf_method_func = v.utils.getFunction(
                "__getInterfaceMethod", get_intf_method_func_ty);
        LLVMValueRef offset_local = LLVMConstInt(
                LLVMInt32TypeInContext(v.context), dispInfo.methodIndex(),
                /* sign-extend */ 0);
        LLVMValueRef funcPtr = v.utils.buildFunCall(
                get_intf_method_func, // ptr to method code
                obj_bitcast, // the object
                intf_id_hash_const, // id hash code
                intf_id_global, // id
                offset_local); // method index

        LLVMTypeRef funcPtrT = v.utils.ptrTypeRef(funcType);
        return LLVMBuildBitCast(v.builder, funcPtr, funcPtrT, "cast.interface.method");
    }

    @Override
    protected LLVMValueRef buildReceiverArg(LLVMTranslator v) {
        return v.getTranslation(node().target());
    }

    @Override
    public Call node() {
        return (Call) super.node();
    }
}
