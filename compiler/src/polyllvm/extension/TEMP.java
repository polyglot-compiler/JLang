package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Special;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;

public class TEMP {

    public TEMP() {
        List<LLVMInstruction> instructions = new ArrayList<>();

        String mangledFuncName =
                PolyLLVMMangler.mangleMethodName(n.methodInstance());
        LLVMTypeNode tn =
                PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
                                                           n.methodInstance()
                                                            .formalTypes(),
                                                           n.methodInstance()
                                                            .returnType());
        LLVMVariable func =
                nf.LLVMVariable(mangledFuncName, tn, VarType.GLOBAL);
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments = new ArrayList<>();

        if (n.target() instanceof Expr) {
            ReferenceType referenceType;
            LLVMOperand thisTranslation;
            LLVMTypeNode thisType;
            LLVMTypeNode functionPtrType;

            if (n.target() instanceof Special
                    && ((Special) n.target()).kind() == Special.SUPER) {
                referenceType = v.getCurrentClass().type();
                thisType =
                        nf.LLVMPointerType(PolyLLVMTypeUtils.polyLLVMObjectVariableType(v,
                                                                                        referenceType));
                thisTranslation = nf.LLVMVariable(PolyLLVMConstants.THISSTRING,
                                                  thisType,
                                                  VarType.LOCAL);
                functionPtrType =
                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                 (ReferenceType) n.target()
                                                                                  .type(),
                                                                 n.methodInstance()
                                                                  .formalTypes(),
                                                                 n.methodInstance()
                                                                  .returnType());

                MethodInstance superMethod =
                        n.methodInstance().overrides().get(0);

                LLVMTypeNode toType =
                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                 v.getCurrentClass()
                                                                  .type(),
                                                                 n.methodInstance()
                                                                  .formalTypes(),
                                                                 n.methodInstance()
                                                                  .returnType());
                LLVMVariable newfunctionPtr =
                        PolyLLVMFreshGen.freshLocalVar(nf, toType);

                LLVMTypeNode superMethodType =
                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                 superMethod.container(),
                                                                 superMethod.formalTypes(),
                                                                 superMethod.returnType());
                LLVMOperand superFunc =
                        nf.LLVMVariable(PolyLLVMMangler.mangleMethodName(superMethod),
                                        superMethodType,
                                        VarType.GLOBAL);
                LLVMConversion castFunction =
                        nf.LLVMConversion(LLVMConversion.BITCAST,
                                          newfunctionPtr,
                                          superMethodType,
                                          superFunc,
                                          toType);
                instructions.add(castFunction);

                func = newfunctionPtr;

            }
            else {
                referenceType = (ReferenceType) n.target().type();
                thisTranslation = (LLVMOperand) v.getTranslation(n.target());
                thisType =
                        PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                           n.target().type());
                functionPtrType =
                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                 referenceType,
                                                                 n.methodInstance()
                                                                  .formalTypes(),
                                                                 n.methodInstance()
                                                                  .returnType());

            }
//            ReferenceType referenceType = (ReferenceType) n.target().type();

//            LLVMOperand thisTranslation =
//                    (LLVMOperand) v.getTranslation(n.target());

            //Get the function from the dispatch vector
            LLVMTypedOperand index0 = nf.LLVMTypedOperand(
                                                          nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                                            0),
                                                          nf.LLVMIntType(32));

            List<LLVMTypedOperand> gepIndexList =
                    CollectionUtil.list(index0, index0);
            LLVMTypeNode dvTypeVariable =
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(v,
                                                                         referenceType);
            LLVMVariable dvDoublePtrResult =
                    PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                        "dvDoublePtrResult",
                                                        nf.LLVMPointerType(dvTypeVariable));
            LLVMInstruction gepDVDoublePtr =
                    nf.LLVMGetElementPtr(thisTranslation, gepIndexList)
                      .result(dvDoublePtrResult);
            instructions.add(gepDVDoublePtr);

            LLVMVariable dvPtrValue =
                    PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                        "dvPtrValue",
                                                        nf.LLVMPointerType(dvTypeVariable));

            LLVMLoad loadDV = nf.LLVMLoad(dvPtrValue,
                                          nf.LLVMPointerType(dvTypeVariable),
                                          dvDoublePtrResult);
            instructions.add(loadDV);

//            if (n.target() instanceof Special
//                    && ((Special) n.target()).kind() == Special.SUPER) {
//                System.out.println("--------------------------------------------------\n"
//                        + "Target of Special call (" + n + ") type: "
//                        + n.target().type() + "\nOverrides : "
//                        + n.methodInstance().overrides()
//                        + "\n--------------------------------------------------");
//
//                MethodInstance superMethod =
//                        n.methodInstance().overrides().get(0);
//
//                LLVMTypeNode toType =
//                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
//                                                                 v.getCurrentClass()
//                                                                  .type(),
//                                                                 n.methodInstance()
//                                                                  .formalTypes(),
//                                                                 n.methodInstance()
//                                                                  .returnType());
//                LLVMVariable newfunctionPtr =
//                        PolyLLVMFreshGen.freshLocalVar(nf, toType);
//
//                LLVMTypeNode superMethodType =
//                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
//                                                                 superMethod.container(),
//                                                                 superMethod.formalTypes(),
//                                                                 superMethod.returnType());
//                LLVMOperand superFunc =
//                        nf.LLVMVariable(PolyLLVMMangler.mangleMethodName(superMethod),
//                                        superMethodType,
//                                        VarType.GLOBAL);
//                LLVMConversion castFunction =
//                        nf.LLVMConversion(LLVMConversion.BITCAST,
//                                          newfunctionPtr,
//                                          superMethodType,
//                                          superFunc,
//                                          toType);
//                instructions.add(castFunction);
//                func = newfunctionPtr;

//                LLVMTypeNode superDvTypeVariable =
//                        PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(v,
//                                                                             (ReferenceType) n.target()
//                                                                                              .type());
//                LLVMVariable superDvDoublePtr =
//                        PolyLLVMFreshGen.freshNamedLocalVar(nf,
//                                                            "superDvDoublePtr",
//                                                            nf.LLVMPointerType(nf.LLVMPointerType(superDvTypeVariable)));
//                LLVMPointerType i8DoublePtrType =
//                        nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8)));
//                LLVMVariable superDvDoublePtr_i8ptr =
//                        PolyLLVMFreshGen.freshNamedLocalVar(nf,
//                                                            "superDvDoublePtr_i8ptr",
//                                                            nf.LLVMPointerType(i8DoublePtrType));
//
//                LLVMInstruction getSuperDV =
//                        nf.LLVMGetElementPtr(dvPtrValue, gepIndexList)
//                          .result(superDvDoublePtr_i8ptr);
//                instructions.add(getSuperDV);
//
//                LLVMConversion bitcast =
//                        nf.LLVMConversion(LLVMConversion.BITCAST,
//                                          superDvDoublePtr,
//                                          i8DoublePtrType,
//                                          superDvDoublePtr_i8ptr,
//                                          nf.LLVMPointerType(nf.LLVMPointerType(superDvTypeVariable)));
//                instructions.add(bitcast);
//
//                dvPtrValue =
//                        PolyLLVMFreshGen.freshNamedLocalVar(nf,
//                                                            "superDvPtrValue",
//                                                            nf.LLVMPointerType(superDvTypeVariable));
//
//                LLVMLoad loadSuperDV = nf.LLVMLoad(dvPtrValue,
//                                                   nf.LLVMPointerType(superDvTypeVariable),
//                                                   superDvDoublePtr);
//                instructions.add(loadSuperDV);
//            }

            int methodIndex =
                    v.getMethodIndex(referenceType, n.methodInstance());

            LLVMTypedOperand indexIntoDV =
                    nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                          methodIndex),
                                        nf.LLVMIntType(32));
            gepIndexList = CollectionUtil.list(index0, indexIntoDV);

            LLVMVariable funcDoublePtr =
                    PolyLLVMFreshGen.freshLocalVar(nf, functionPtrType);
            LLVMInstruction funcPtrInstruction =
                    nf.LLVMGetElementPtr(dvPtrValue, gepIndexList)
                      .result(funcDoublePtr);
            instructions.add(funcPtrInstruction);

            LLVMVariable functionPtr =
                    PolyLLVMFreshGen.freshLocalVar(nf, functionPtrType);

            LLVMLoad loadFunctionPtr =
                    nf.LLVMLoad(functionPtr, functionPtrType, funcDoublePtr);
            instructions.add(loadFunctionPtr);

            func = functionPtr;

//            if (n.target() instanceof Special
//                    && ((Special) n.target()).kind() == Special.SUPER) {
//
//                LLVMTypeNode toType =
//                        PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
//                                                                 v.getCurrentClass()
//                                                                  .type(),
//                                                                 n.methodInstance()
//                                                                  .formalTypes(),
//                                                                 n.methodInstance()
//                                                                  .returnType());
//                LLVMVariable newfunctionPtr =
//                        PolyLLVMFreshGen.freshLocalVar(nf, toType);
//
//                LLVMConversion castFunction =
//                        nf.LLVMConversion(LLVMConversion.BITCAST,
//                                          newfunctionPtr,
//                                          functionPtrType,
//                                          func,
//                                          toType);
//                instructions.add(castFunction);
//                func = newfunctionPtr;
//
//            }

//            instructions.add(methodPtr);

            //Add this as an argument
            if (thisTranslation instanceof LLVMESeq) {

                arguments.add(new Pair<>(thisType,
                                         ((LLVMESeq) thisTranslation).expr()));
            }
            else {
                arguments.add(new Pair<>(thisType, thisTranslation));

            }

        }

        for (Expr arg : n.arguments()) {
            if (arg == null) throw new InternalCompilerError("The argument "
                    + arg + " to function " + n.methodInstance()
                    + "is not translated");
            LLVMTypeNode typeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, arg.type());
            LLVMOperand operand = (LLVMOperand) v.getTranslation(arg);
            arguments.add(new Pair<>(typeNode, operand));
        }
        LLVMCall llvmCall =
                nf.LLVMCall(func,
                            arguments,
                            PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type()));
        LLVMTypeNode retTn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                                n.methodInstance()
                                                                 .returnType());
        LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, retTn);
        if (!n.type().isVoid()) {
            llvmCall = llvmCall.result(result);
        }

        if (!(n.target() instanceof Expr)) {
            v.addStaticCall(llvmCall);
        }
        instructions.add(llvmCall);
        v.addTranslation(n, nf.LLVMESeq(nf.LLVMSeq(instructions), result));
        return super.translatePseudoLLVM(v);
    }

}
