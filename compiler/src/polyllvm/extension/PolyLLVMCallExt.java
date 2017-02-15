package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable.VarKind;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Call n = (Call) node();
        NodeFactory nf = v.nodeFactory();
        List<Expr> args = new ArrayList<>();
        List<? extends Type> types = n.methodInstance().formalTypes();
        for (int i = 0; i < n.arguments().size(); i++) {
            Expr expr = n.arguments().get(i);
            Type t = types.get(i);
            if (!t.equals(expr.type())) {
                CanonicalTypeNode castTypeNode =
                        nf.CanonicalTypeNode(Position.compilerGenerated(), t);
                Expr cast = nf.Cast(Position.compilerGenerated(),
                                    castTypeNode,
                                    expr)
                              .type(t);
                args.add(cast);
            }
            else {
                args.add(expr);
            }
        }
        return n.arguments(args);
    }

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
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();
        LLVMPointerType bytePointerType = nf.LLVMPointerType(nf.LLVMIntType(8));

        ReferenceType referenceType = (ReferenceType) n.target().type();
        LLVMOperand thisTranslation = v.getTranslation(n.target());
        LLVMTypeNode thisType =
                LLVMUtils.polyLLVMTypeNode(nf, n.target().type());
        LLVMTypeNode functionPtrType =
                LLVMUtils.polyLLVMMethodTypeNode(nf,
                        referenceType,
                        n.methodInstance()
                                .formalTypes(),
                        n.methodInstance()
                                .returnType());

        int methodIndex = v.getMethodIndex(referenceType, n.methodInstance());

        LLVMArrayType arrayType = nf.LLVMArrayType(nf.LLVMIntType(8), referenceType.toString().length() + 1);
        LLVMVariable interfaceStringPtr = nf.LLVMVariable(PolyLLVMMangler.interfaceStringVariable(referenceType),
                nf.LLVMPointerType(arrayType), VarKind.GLOBAL);
        LLVMVariable interfaceStringBytePointer =
                PolyLLVMFreshGen.freshNamedLocalVar(nf, "interface_string", bytePointerType);
        LLVMInstruction toBytePtr =
                PolyLLVMFreshGen.freshGetElementPtr(nf,interfaceStringBytePointer,interfaceStringPtr,0,0);

        LLVMVariable thisBytePointer =
                PolyLLVMFreshGen.freshLocalVar(nf, bytePointerType);
        LLVMConversion castThisBytePointer = nf.LLVMConversion(LLVMConversion.BITCAST, thisBytePointer,
                thisType, thisTranslation, bytePointerType);

        //void* __getInterfaceMethod(jobject* o, char* interface_string, int methodIndex)
        List<LLVMTypeNode> argumentTypes = CollectionUtil.list(bytePointerType, bytePointerType, nf.LLVMIntType(32));
        LLVMFunctionType getInterfaceMethodFunctionType = nf.LLVMFunctionType(argumentTypes, bytePointerType);
        List<Pair<LLVMTypeNode, LLVMOperand>> argsGetMethod = CollectionUtil.list(
                new Pair<>(bytePointerType, nf.LLVMESeq(castThisBytePointer, thisBytePointer)),
                new Pair<>(bytePointerType, nf.LLVMESeq(toBytePtr,interfaceStringBytePointer)),
                new Pair<>(nf.LLVMIntType(32), nf.LLVMIntLiteral(nf.LLVMIntType(32),methodIndex)));
        LLVMVariable functionPtrTemp =
                PolyLLVMFreshGen.freshLocalVar(nf, bytePointerType);
        LLVMCall getInterfaceMethod = nf.LLVMCall(
                nf.LLVMVariable("__getInterfaceMethod", getInterfaceMethodFunctionType, VarKind.GLOBAL),
                argsGetMethod, bytePointerType).result(functionPtrTemp);
        v.addStaticCall(getInterfaceMethod);

        LLVMVariable functionPtr =
                PolyLLVMFreshGen.freshLocalVar(nf, functionPtrType);
        LLVMConversion bitcast = nf.LLVMConversion(LLVMConversion.BITCAST, functionPtr, bytePointerType,
                nf.LLVMESeq(getInterfaceMethod, functionPtrTemp), functionPtrType);
        instructions.add(bitcast);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf, thisTranslation, thisType);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, functionPtr, arguments, true);
        instructions.add(pair.part1());
        v.addTranslation(n, nf.LLVMESeq(nf.LLVMSeq(instructions), pair.part2()));

    }


}
