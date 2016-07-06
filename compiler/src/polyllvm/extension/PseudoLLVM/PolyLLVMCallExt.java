package polyllvm.extension.PseudoLLVM;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;

public class PolyLLVMCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Override
//    public Node llvmVarToStack(LLVMVarToStack v) {
//        System.out.println("LLVMCALL VAR2STACK PASS");
//
//        LLVMCall n = (LLVMCall) node();
//        PolyLLVMNodeFactory nf = v.nodeFactory();
//
//        List<LLVMInstruction> instrs = new ArrayList<>();
//        List<Pair<LLVMTypeNode, LLVMOperand>> newArgs = new ArrayList<>();
//        for (Pair<LLVMTypeNode, LLVMOperand> p : n.arguments()) {
//            if (p.part2() instanceof LLVMVariable) {
//                String name = ((LLVMVariable) p.part2()).name();
//                if (!v.isArg(name)) {
//                    v.addAllocation(name, p.part1());
//                }
//                LLVMVariable temp = PolyLLVMFreshGen.freshLocalVar(nf);
//                LLVMVariable var =
//                        nf.LLVMVariable(Position.compilerGenerated(),
//                                        v.varName(name),
//                                        VarType.LOCAL);
//                LLVMLoad load = nf.LLVMLoad(Position.compilerGenerated(),
//                                            temp,
//                                            p.part1(),
//                                            var);
//                instrs.add(load);
//                newArgs.add(new Pair<LLVMTypeNode, LLVMOperand>(p.part1(),
//                                                                temp));
//            }
//            else {
//                newArgs.add(p);
//            }
//        }
//
//        if (n.result() != null) {
//            LLVMVariable currResult =
//                    nf.LLVMVariable(Position.compilerGenerated(),
//                                    v.varName(n.result().name()),
//                                    VarType.LOCAL);//n.result();
//            LLVMVariable resultTemp = PolyLLVMFreshGen.freshLocalVar(nf);
//
//            n = n.result(resultTemp);
//            n = n.arguments(newArgs);
//
//            instrs.add(n);
//            LLVMStore store = nf.LLVMStore(Position.compilerGenerated(),
//                                           n.retType(),
//                                           resultTemp,
//                                           currResult);
//            instrs.add(store);
//        }
//        else {
//            n = n.arguments(newArgs);
//            instrs.add(n);
//        }
//
//        return nf.LLVMSeq(Position.compilerGenerated(), instrs);
//    }

//
//    n = n.result(resultTemp);
//    instrs.add(n);
//    LLVMStore store = nf.LLVMStore(Position.compilerGenerated(),
//                                   n.typeNode(),
//                                   resultTemp,
//                                   currResult);
//    instrs.add(store);

}
