package polyllvm.extension;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.LLVMUtils;
import polyllvm.util.Constants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMConstructorCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ConstructorCall n = (ConstructorCall) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.qualifier() != null) {
            throw new InternalCompilerError("Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        LLVMOperand thisArg;
        LLVMTypeNode thisArgType;

        if (n.kind() == ConstructorCall.THIS) {
            thisArgType = LLVMUtils.polyLLVMTypeNode(nf,
                                                             n.constructorInstance()
                                                              .container());
            thisArg = nf.LLVMVariable(Constants.THIS_STR,
                                      thisArgType,
                                      LLVMVariable.VarKind.LOCAL);
        }
        else if (n.kind() == ConstructorCall.SUPER) {
            LLVMTypeNode thisType =
                    LLVMUtils.polyLLVMTypeNode(nf,
                                                       v.getCurrentClass()
                                                        .type());
            LLVMTypeNode superType =
                    LLVMUtils.polyLLVMTypeNode(nf,
                                                       n.constructorInstance()
                                                        .container());
            LLVMVariable thisVariable =
                    nf.LLVMVariable(Constants.THIS_STR,
                                    thisType,
                                    LLVMVariable.VarKind.LOCAL);
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
                LLVMUtils.polyLLVMMethodTypeNode(nf,
                                                         n.constructorInstance()
                                                          .container(),
                                                         n.constructorInstance()
                                                          .formalTypes());
        LLVMVariable func =
                nf.LLVMVariable(mangledFuncName, tn, LLVMVariable.VarKind.GLOBAL);

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
