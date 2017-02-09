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
import polyllvm.util.Constants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

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
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();

        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.methodInstance());
        LLVMTypeNode tn =
                LLVMUtils.polyLLVMFunctionTypeNode(nf,
                                                           n.methodInstance()
                                                            .formalTypes(),
                                                           n.methodInstance()
                                                            .returnType());
        LLVMVariable func =
                nf.LLVMVariable(mangledFuncName, tn, LLVMVariable.VarKind.GLOBAL);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, func, arguments, false);
        instructions.add(pair.part1());
        v.addTranslation(n,
                         nf.LLVMESeq(nf.LLVMSeq(instructions), pair.part2()));
    }

    private void translateSuperCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();

        MethodInstance superMethod = n.methodInstance().overrides().get(0);

        LLVMTypeNode toType =
                LLVMUtils.polyLLVMMethodTypeNode(nf,
                                                         v.getCurrentClass()
                                                          .type(),
                                                         n.methodInstance()
                                                          .formalTypes(),
                                                         n.methodInstance()
                                                          .returnType());

        LLVMTypeNode superMethodType =
                LLVMUtils.polyLLVMMethodTypeNode(nf,
                                                         superMethod.container(),
                                                         superMethod.formalTypes(),
                                                         superMethod.returnType());
        LLVMVariable superMethodPtr =
                nf.LLVMVariable(PolyLLVMMangler.mangleProcedureName(superMethod),
                                superMethodType,
                                LLVMVariable.VarKind.GLOBAL);

        LLVMVariable superMethodCastPtr =
                PolyLLVMFreshGen.freshLocalVar(nf, toType);

        LLVMConversion castFunction =
                nf.LLVMConversion(LLVMConversion.BITCAST,
                                  superMethodCastPtr,
                                  superMethodType,
                                  superMethodPtr,
                                  toType);
        instructions.add(castFunction);

        LLVMTypeNode thisType =
                LLVMUtils.polyLLVMTypeNode(nf,
                                                   v.getCurrentClass().type());
        LLVMOperand thisTranslation =
                nf.LLVMVariable(Constants.THIS_STR,
                                thisType,
                                VarKind.LOCAL);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf, thisTranslation, thisType);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, superMethodCastPtr, arguments, true);
        instructions.add(pair.part1());
        v.addTranslation(n,
                         nf.LLVMESeq(nf.LLVMSeq(instructions), pair.part2()));

        LLVMTypeNode superTypeNode =
                LLVMUtils.polyLLVMTypeNode(nf, superMethod.container());
        LLVMOperand superTranslation =
                nf.LLVMVariable(PolyLLVMFreshGen.freshNamedLabel(nf, "argument")
                                                .name(),
                                superTypeNode,
                                LLVMVariable.VarKind.LOCAL);

        arguments = setupArguments(v, n, nf, superTranslation, superTypeNode);
        LLVMTypeNode retType = LLVMUtils.polyLLVMTypeNode(nf, n.type());
        v.addStaticCall(nf.LLVMCall(superMethodPtr, arguments, retType));

    }

    private void translateMethodCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();

        ReferenceType referenceType = (ReferenceType) n.target().type();
        LLVMOperand thisTranslation =
                (LLVMOperand) v.getTranslation(n.target());
        LLVMTypeNode thisType =
                LLVMUtils.polyLLVMTypeNode(nf, n.target().type());
        LLVMTypeNode functionPtrType =
                LLVMUtils.polyLLVMMethodTypeNode(nf,
                                                         referenceType,
                                                         n.methodInstance()
                                                          .formalTypes(),
                                                         n.methodInstance()
                                                          .returnType());

        LLVMTypeNode dvTypeVariable =
                LLVMUtils.polyLLVMDispatchVectorVariableType(v,
                                                                     referenceType);
        LLVMVariable dvDoublePtrResult =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "dvDoublePtrResult",
                                                    nf.LLVMPointerType(dvTypeVariable));
        LLVMInstruction gepDVDoublePtr =
                PolyLLVMFreshGen.freshGetElementPtr(nf,
                                                    dvDoublePtrResult,
                                                    thisTranslation,
                                                    0,
                                                    0);
        instructions.add(gepDVDoublePtr);

        LLVMVariable dvPtrValue =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "dvPtrValue",
                                                    nf.LLVMPointerType(dvTypeVariable));

        LLVMLoad loadDV = nf.LLVMLoad(dvPtrValue,
                                      nf.LLVMPointerType(dvTypeVariable),
                                      dvDoublePtrResult);
        instructions.add(loadDV);

        int methodIndex = v.getMethodIndex(referenceType, n.methodInstance());

        LLVMVariable funcDoublePtr =
                PolyLLVMFreshGen.freshLocalVar(nf, functionPtrType);

        LLVMInstruction funcPtrInstruction =
                PolyLLVMFreshGen.freshGetElementPtr(nf,
                                                    funcDoublePtr,
                                                    dvPtrValue,
                                                    0,
                                                    methodIndex);
        instructions.add(funcPtrInstruction);

        LLVMVariable functionPtr =
                PolyLLVMFreshGen.freshLocalVar(nf, functionPtrType);

        LLVMLoad loadFunctionPtr =
                nf.LLVMLoad(functionPtr, functionPtrType, funcDoublePtr);
        instructions.add(loadFunctionPtr);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf, thisTranslation, thisType);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, functionPtr, arguments, true);
        instructions.add(pair.part1());
        v.addTranslation(n,
                         nf.LLVMESeq(nf.LLVMSeq(instructions), pair.part2()));

    }

    private void translateInterfaceMethodCall(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();
        LLVMPointerType bytePointerType = nf.LLVMPointerType(nf.LLVMIntType(8));

        ReferenceType referenceType = (ReferenceType) n.target().type();
        LLVMOperand thisTranslation =
                (LLVMOperand) v.getTranslation(n.target());
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
