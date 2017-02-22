package polyllvm.extension;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Call n = (Call) node();

        if (n.target() instanceof Special
                && ((Special) n.target()).kind() == Special.SUPER) {
            translateSuperCall(v);
        }
        else if (n.target() instanceof Expr && v.isInterfaceCall(n.methodInstance())) {
            translateInterfaceMethodCall(v);
        }
        else if (n.target() instanceof Expr) {
            translateMethodCall(v);
        }
        else {
            translateStaticCall(v);
        }

        return super.translatePseudoLLVM(v);
    }

    private void translateStaticCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();

        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.methodInstance());

        LLVMTypeRef tn = LLVMUtils.functionType(n.methodInstance().returnType(), n.methodInstance().formalTypes(), v);

        LLVMValueRef[] args = n.arguments().stream()
                .map(v::getTranslation)
                .toArray(LLVMValueRef[]::new);

        LLVMValueRef func = LLVMUtils.getFunction(v.mod, mangledFuncName, tn);
        if(n.methodInstance().returnType().isVoid()){
            v.addTranslation(n, LLVMUtils.buildProcedureCall(v.builder, func, args));
        } else{
            v.addTranslation(n, LLVMUtils.buildMethodCall(v.builder, func, args));
        }

    }

    private void translateSuperCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        MethodInstance superMethod = n.methodInstance().overrides().get(0);

        LLVMTypeRef toType = LLVMUtils.methodType(v.getCurrentClass().type(),
                n.methodInstance().returnType(), n.methodInstance().formalTypes(),
                v);

        LLVMTypeRef superMethodType = LLVMUtils.methodType(
                superMethod.container(),
                superMethod.returnType(), superMethod.formalTypes(),
                v);

        LLVMValueRef superFunc = LLVMUtils.getFunction(v.mod,
                PolyLLVMMangler.mangleProcedureName(superMethod),
                superMethodType);

        LLVMValueRef superBitCast = LLVMBuildBitCast(v.builder, superFunc, toType,"bitcast_super");


        LLVMValueRef thisArg = LLVMGetParam(v.currFn(), 0);

        LLVMValueRef[] args =
                Stream.concat(
                    Stream.of(thisArg),
                    n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);

        if(n.methodInstance().returnType().isVoid()){
            v.addTranslation(n, LLVMUtils.buildProcedureCall(v.builder, superBitCast, args));
        } else{
            v.addTranslation(n, LLVMUtils.buildMethodCall(v.builder, superBitCast, args));
        }

    }

    private void translateMethodCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();

        ReferenceType referenceType = (ReferenceType) n.target().type();
        LLVMValueRef thisTranslation = v.getTranslation(n.target());

        LLVMValueRef dvDoublePtr = LLVMUtils.buildGEP(v.builder, thisTranslation,
                LLVMConstInt(LLVMInt32Type(), 0, /* sign-extend */ 0),
                LLVMConstInt(LLVMInt32Type(), 0, /* sign-extend */ 0));

        LLVMValueRef dvPtr = LLVMBuildLoad(v.builder, dvDoublePtr, "dv_ptr");

        int methodIndex = v.getMethodIndex(referenceType, n.methodInstance());

        LLVMTypeRef res = LLVMGetTypeByName(v.mod, PolyLLVMMangler.dispatchVectorTypeName(referenceType));
        LLVMTypeRef methodType = LLVMStructGetTypeAtIndex(res, methodIndex);
        int i = LLVMGetPointerAddressSpace(LLVMTypeOf(dvPtr));
        LLVMValueRef funcDoublePtr = LLVMUtils.buildGEP(v.builder, dvPtr,
                LLVMConstInt(LLVMInt32Type(), 0, /* sign-extend */ 0),
                LLVMConstInt(LLVMInt32Type(), methodIndex, /* sign-extend */ 0));

        LLVMValueRef methodPtr = LLVMBuildLoad(v.builder, funcDoublePtr, "load_method_ptr");

        LLVMValueRef[] args = Stream.concat(
                Stream.of(thisTranslation),
                n.arguments().stream().map(arg -> v.getTranslation(arg))
            ).toArray(LLVMValueRef[]::new);

        if(n.methodInstance().returnType().isVoid()){
            v.addTranslation(n,LLVMUtils.buildProcedureCall(v.builder, methodPtr, args));
        } else {
            v.addTranslation(n,LLVMUtils.buildMethodCall(v.builder, methodPtr, args));

        }
    }

    private void translateInterfaceMethodCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        ReferenceType rt = (ReferenceType) n.target().type();
        MethodInstance mi = n.methodInstance();
        LLVMValueRef thisTranslation = v.getTranslation(n.target());

        LLVMTypeRef methodType = LLVMUtils.ptrTypeRef(LLVMUtils.methodType(rt, mi.returnType(), mi.formalTypes(), v));

        int methodIndex = v.getMethodIndex(rt, mi);

        LLVMValueRef interfaceStringPtr = LLVMUtils.getGlobal(v.mod, PolyLLVMMangler.interfaceStringVariable(rt),
                LLVMArrayType(LLVMInt8Type(), rt.toString().length() + 1));
        LLVMValueRef interfaceStringBytePtr = LLVMUtils.buildGEP(v.builder, interfaceStringPtr,
                LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 0, 0));

        LLVMValueRef obj_bitcast = LLVMBuildBitCast(v.builder, thisTranslation,
                LLVMUtils.ptrTypeRef(LLVMInt8Type()), "cast_for_interface_call");

        LLVMTypeRef getInterfaceMethodType = LLVMUtils.functionType(
                LLVMUtils.ptrTypeRef(LLVMInt8Type()), // void* return type
                LLVMUtils.ptrTypeRef(LLVMInt8Type()), // jobject*
                LLVMUtils.ptrTypeRef(LLVMInt8Type()), // char*
                LLVMInt32Type()                       // int
        );
        LLVMValueRef getInterfaceMethod = LLVMUtils.getFunction(v.mod, "__getInterfaceMethod", getInterfaceMethodType);
        LLVMValueRef interfaceMethod = LLVMUtils.buildMethodCall(v.builder, getInterfaceMethod,
                obj_bitcast, interfaceStringBytePtr, LLVMConstInt(LLVMInt32Type(), methodIndex, /* sign-extend */ 0));

        LLVMValueRef cast = LLVMBuildBitCast(v.builder, interfaceMethod, methodType, "cast_interface_method");


        LLVMValueRef[] args =
                Stream.concat(
                        Stream.of(thisTranslation),
                        n.arguments().stream().map(v::getTranslation))
                        .toArray(LLVMValueRef[]::new);

        if(n.methodInstance().returnType().isVoid()){
            v.addTranslation(n, LLVMUtils.buildProcedureCall(v.builder, cast, args));
        } else{
            v.addTranslation(n, LLVMUtils.buildMethodCall(v.builder, cast, args));
        }
    }


}
