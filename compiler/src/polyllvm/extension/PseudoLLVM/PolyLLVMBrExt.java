package polyllvm.extension.PseudoLLVM;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;

public class PolyLLVMBrExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Override
//    public Node llvmVarToStack(LLVMVarToStack v) {
//        LLVMBr n = (LLVMBr) node();
//        PolyLLVMNodeFactory nf = v.nodeFactory();
//        List<LLVMInstruction> instrs = new ArrayList<>();
//        if (n.cond() != null && n.cond().operand() instanceof LLVMVariable) {
//            LLVMOperand operand = n.cond().operand();
//            String name = ((LLVMVariable) operand).name();
//            if (!v.isArg(name)) {
//                v.addAllocation(name, n.cond().typeNode());
//            }
//            LLVMVariable temp = PolyLLVMFreshGen.freshLocalVar(nf);
//            LLVMVariable var = nf.LLVMVariable(Position.compilerGenerated(),
//                                               v.varName(name),
//                                               VarType.LOCAL);
//            LLVMLoad load = nf.LLVMLoad(Position.compilerGenerated(),
//                                        temp,
//                                        n.cond().typeNode(),
//                                        var);
//            instrs.add(load);
//            n = n.cond(nf.LLVMTypedOperand(Position.compilerGenerated(),
//                                           temp,
//                                           n.cond().typeNode()));
//        }
//        instrs.add(n);
//
//        LLVMSeq llvmSeq = nf.LLVMSeq(Position.compilerGenerated(), instrs);
////        System.out.println("\n");
////        llvmSeq.prettyPrint(nf.lang(), System.out);
//        return llvmSeq;
//
//    }
}
