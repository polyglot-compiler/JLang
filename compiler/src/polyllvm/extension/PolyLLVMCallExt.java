package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
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
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Call n = (Call) node();
        NodeFactory nf = v.nodeFactory();
        List<Expr> args = new ArrayList<>();
//      List<? extends Type> types = n.methodInstance().formalTypes();
        List<Type> types = new ArrayList<>();
        for (Expr arg : n.arguments()) {
            types.add(arg.type());
        }
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
                PolyLLVMMangler.mangleMethodName(n.methodInstance());
        LLVMTypeNode tn =
                PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
                                                           n.methodInstance()
                                                            .formalTypes(),
                                                           n.methodInstance()
                                                            .returnType());
        LLVMVariable func =
                nf.LLVMVariable(mangledFuncName, tn, VarType.GLOBAL);

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
                PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                         v.getCurrentClass()
                                                          .type(),
                                                         n.methodInstance()
                                                          .formalTypes(),
                                                         n.methodInstance()
                                                          .returnType());

        LLVMTypeNode superMethodType =
                PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                         superMethod.container(),
                                                         superMethod.formalTypes(),
                                                         superMethod.returnType());
        LLVMVariable superMethodPtr =
                nf.LLVMVariable(PolyLLVMMangler.mangleMethodName(superMethod),
                                superMethodType,
                                VarType.GLOBAL);

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
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                   v.getCurrentClass().type());
        LLVMOperand thisTranslation =
                nf.LLVMVariable(PolyLLVMConstants.THISSTRING,
                                thisType,
                                VarType.LOCAL);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf, thisTranslation, thisType);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, superMethodCastPtr, arguments, true);
        instructions.add(pair.part1());
        v.addTranslation(n,
                         nf.LLVMESeq(nf.LLVMSeq(instructions), pair.part2()));

        LLVMTypeNode superTypeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, superMethod.container());
        LLVMOperand superTranslation =
                nf.LLVMVariable(PolyLLVMFreshGen.freshNamedLabel(nf, "argument")
                                                .name(),
                                superTypeNode,
                                VarType.LOCAL);

        arguments = setupArguments(v, n, nf, superTranslation, superTypeNode);
        LLVMTypeNode retType = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
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
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.target().type());
        LLVMTypeNode functionPtrType =
                PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                         referenceType,
                                                         n.methodInstance()
                                                          .formalTypes(),
                                                         n.methodInstance()
                                                          .returnType());

        LLVMTypeNode dvTypeVariable =
                PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(v,
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

    private List<Pair<LLVMTypeNode, LLVMOperand>> setupArguments(
            PseudoLLVMTranslator v, Call n, PolyLLVMNodeFactory nf,
            LLVMOperand thisTranslation, LLVMTypeNode thisType) {
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf);

        //Add this as the first argument
        if (thisTranslation instanceof LLVMESeq) {
            arguments.add(0, new Pair<>(thisType,
                                        ((LLVMESeq) thisTranslation).expr()));
        }
        else {
            arguments.add(0, new Pair<>(thisType, thisTranslation));
        }

        return arguments;
    }

    private List<Pair<LLVMTypeNode, LLVMOperand>> setupArguments(
            PseudoLLVMTranslator v, Call n, PolyLLVMNodeFactory nf) {
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments = new ArrayList<>();

        for (Expr arg : n.arguments()) {
            if (arg == null) throw new InternalCompilerError("The argument "
                    + arg + " to function " + n.methodInstance()
                    + "is not translated");
            LLVMTypeNode typeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, arg.type());
            LLVMOperand operand = (LLVMOperand) v.getTranslation(arg);
            arguments.add(new Pair<>(typeNode, operand));
        }

        return arguments;
    }

    private Pair<LLVMCall, LLVMVariable> setupCall(PseudoLLVMTranslator v,
            Call n, PolyLLVMNodeFactory nf, LLVMVariable functionPtr,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments, boolean isMethod) {

        LLVMTypeNode callTypeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        LLVMCall llvmCall = nf.LLVMCall(functionPtr, arguments, callTypeNode);
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

        return new Pair<>(llvmCall, result);
    }
}
