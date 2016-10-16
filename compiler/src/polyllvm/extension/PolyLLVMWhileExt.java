package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.While;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMWhileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        v.enterLoop((While) node());
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        While n = (While) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        Pair<String, String> labels = v.leaveLoop();

        LLVMLabel head = nf.LLVMLabel(labels.part1());
        LLVMLabel end = nf.LLVMLabel(labels.part2());
        LLVMLabel l1 = PolyLLVMFreshGen.freshLabel(nf);

        List<LLVMInstruction> instrs = new ArrayList<>();
        instrs.add(nf.LLVMBr(head));
        instrs.add(nf.LLVMSeqLabel(head));
        instrs.add((LLVMInstruction) lang().translatePseudoLLVMConditional(n.cond(),
                                                                           v,
                                                                           l1,
                                                                           end));
        instrs.add(nf.LLVMSeqLabel(l1));
        LLVMBlock bodyTranslation = (LLVMBlock) v.getTranslation(n.body());
        instrs.add(bodyTranslation.instructions(nf));
        instrs.add(nf.LLVMBr(head));
        instrs.add(nf.LLVMSeqLabel(end));

        LLVMSeq seq = nf.LLVMSeq(instrs);

        v.addTranslation(n, seq);

        return super.translatePseudoLLVM(v);
    }
}
