package polyllvm.extension.PseudoLLVM;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;

public class PolyLLVMBinaryOperandInstructionExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Override
//    public Node llvmVarToStack(LLVMVarToStack v) {
//        LLVMBinaryOperandInstruction n = (LLVMBinaryOperandInstruction) node();
//        PolyLLVMNodeFactory nf = v.nodeFactory();
//        List<LLVMInstruction> instrs = new ArrayList<>();
//
//        if (n.left() instanceof LLVMVariable) {
//            String leftName = ((LLVMVariable) n.left()).name();
//            if (!v.isArg(leftName)) {
//                v.addAllocation(leftName, n.typeNode());
//            }
//            LLVMVariable leftTemp = PolyLLVMFreshGen.freshLocalVar(nf);
//            LLVMVariable leftVar =
//                    nf.LLVMVariable(Position.compilerGenerated(),
//                                    v.varName(leftName),
//                                    VarType.LOCAL);
//            LLVMLoad loadLeft = nf.LLVMLoad(Position.compilerGenerated(),
//                                            leftTemp,
//                                            n.typeNode(),
//                                            leftVar);
//            instrs.add(loadLeft);
//            n = n.left(leftTemp);
//        }
//        if (n.right() instanceof LLVMVariable) {
//            String rightName = ((LLVMVariable) n.right()).name();
//            if (!v.isArg(rightName)) {
//                v.addAllocation(rightName, n.typeNode());
//            }
//            LLVMVariable rightTemp = PolyLLVMFreshGen.freshLocalVar(nf);
//            LLVMVariable rightVar =
//                    nf.LLVMVariable(Position.compilerGenerated(),
//                                    v.varName(rightName),
//                                    VarType.LOCAL);
//            LLVMLoad loadRight = nf.LLVMLoad(Position.compilerGenerated(),
//                                             rightTemp,
//                                             n.typeNode(),
//                                             rightVar);
//            instrs.add(loadRight);
//            n = n.right(rightTemp);
//        }
//
//        LLVMVariable currResult =
//                nf.LLVMVariable(Position.compilerGenerated(),
//                                v.varName(n.result().name()),
//                                VarType.LOCAL);//n.result();
//        LLVMVariable resultTemp = PolyLLVMFreshGen.freshLocalVar(nf);
//
//        n = n.result(resultTemp);
//        instrs.add(n);
//        LLVMStore store = nf.LLVMStore(Position.compilerGenerated(),
//                                       n.retType(),
//                                       resultTemp,
//                                       currResult);
//        instrs.add(store);
//        return nf.LLVMSeq(Position.compilerGenerated(), instrs);
//    }

}

//%x = add i32 %x, %y
//        |
//        |
//        v
//%temp.2 = load i32, i32* %x
//%temp.3 = load i32, i32* %yarg
//%temp.4 = add i32 %temp.2, %temp.3
//store i32 %temp.4, i32* %x
