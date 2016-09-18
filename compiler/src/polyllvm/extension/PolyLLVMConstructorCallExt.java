package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMConstructorCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ConstructorCall n = (ConstructorCall) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        System.out.println("Constructor Instance for " + n + " : "
                + n.constructorInstance());

        if (n.qualifier() != null) {
            throw new InternalCompilerError("Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        LLVMOperand thisArg;
        LLVMTypeNode thisArgType;

        if (n.kind() == ConstructorCall.THIS) {
            thisArgType = PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                             n.constructorInstance()
                                                              .container());
            thisArg = nf.LLVMVariable(PolyLLVMConstants.THISSTRING,
                                      thisArgType,
                                      VarType.LOCAL);
        }
        else if (n.kind() == ConstructorCall.SUPER) {
            LLVMTypeNode thisType =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                       v.getCurrentClass()
                                                        .type());
            LLVMTypeNode superType =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                       n.constructorInstance()
                                                        .container());
            LLVMVariable thisVariable =
                    nf.LLVMVariable(PolyLLVMConstants.THISSTRING,
                                    thisType,
                                    VarType.LOCAL);
            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, superType);
            LLVMInstruction cast =
                    nf.LLVMConversion(LLVMConversion.BITCAST,
                                      thisType,
                                      thisVariable,
                                      superType)
                      .result(result);
            thisArg = nf.LLVMESeq(cast, result);
            thisArgType = superType;
        }
        else {
            throw new InternalCompilerError("Kind `" + n.kind()
                    + "` of constructor call not handled: " + n);
        }

        translateStaticCall(v, thisArg, thisArgType);
        return n;
    }

    private void translateStaticCall(PseudoLLVMTranslator v,
            LLVMOperand thisArg, LLVMTypeNode thisArgType) {
        ConstructorCall n = (ConstructorCall) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> instructions = new ArrayList<>();

        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.constructorInstance());
        LLVMTypeNode tn =
                PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                         n.constructorInstance()
                                                          .container(),
                                                         n.constructorInstance()
                                                          .formalTypes());
        LLVMVariable func =
                nf.LLVMVariable(mangledFuncName, tn, VarType.GLOBAL);

        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf, thisArg, thisArgType);
        Pair<LLVMCall, LLVMVariable> pair =
                setupCall(v, n, nf, func, arguments, false);

        if (thisArg instanceof LLVMESeq) {
            instructions.add(((LLVMESeq) thisArg).instruction());
        }
        instructions.add(pair.part1());
        v.addTranslation(n, nf.LLVMSeq(instructions));
        v.addStaticCall(pair.part1());
    }

}
