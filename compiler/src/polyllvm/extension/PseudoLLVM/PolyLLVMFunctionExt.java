package polyllvm.extension.PseudoLLVM;

import java.util.List;

import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.visit.LLVMVarToStack;

public class PolyLLVMFunctionExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node llvmVarToStack(LLVMVarToStack v) {
        LLVMFunction n = (LLVMFunction) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        List<LLVMBlock> bs = n.blocks();
        LLVMBlock firstBlock = bs.get(0);
        List<LLVMInstruction> firstBlockInstructions =
                firstBlock.instructions();

//        if (firstBlockInstructions.get(0) instanceof LLVMSeqLabel) {
//            String labelName =
//                    ((LLVMSeqLabel) firstBlockInstructions.get(0)).name();
//            LLVMLabel label =
//                    nf.LLVMLabel(Position.compilerGenerated(), labelName);
//            firstBlockInstructions.add(0,
//                                       nf.LLVMBr(Position.compilerGenerated(),
//                                                 label));
//        }

        firstBlockInstructions.addAll(0, v.allocationInstructions());

        bs.remove(0);
        bs.add(0, firstBlock.instructions(firstBlockInstructions));

        v.clearAllocations();
        v.clearArguments();

        return n.blocks(bs);
    }
}
