package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.If;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMIfExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        If n = (If) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMLabel trueLabel = PolyLLVMFreshGen.freshLabel(v.nodeFactory());
        LLVMLabel falseLabel = PolyLLVMFreshGen.freshLabel(v.nodeFactory());
        LLVMLabel endLabel = PolyLLVMFreshGen.freshLabel(v.nodeFactory());
        LLVMInstruction cond =
                (LLVMInstruction) lang().translatePseudoLLVMConditional(n.cond(),
                                                                        v,
                                                                        trueLabel,
                                                                        falseLabel);
        List<LLVMInstruction> instructions = new ArrayList<>();
        instructions.add(cond);
        instructions.add(nf.LLVMSeqLabel(trueLabel));
        instructions.add(((LLVMBlock) v.getTranslation(n.consequent())).instructions(nf));
        instructions.add(nf.LLVMBr(endLabel));
        instructions.add(nf.LLVMSeqLabel(falseLabel));
        if (n.alternative() != null) {
            instructions.add(((LLVMBlock) v.getTranslation(n.alternative())).instructions(nf));
        }
        instructions.add(nf.LLVMBr(endLabel));
        instructions.add(nf.LLVMSeqLabel(endLabel));

        LLVMSeq translation = nf.LLVMSeq(instructions);

        v.addTranslation(n, translation);
        return super.translatePseudoLLVM(v);
    }

}
