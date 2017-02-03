package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import static org.bytedeco.javacpp.LLVM.*;
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

        Pair<LLVMBasicBlockRef, LLVMBasicBlockRef> labels = v.leaveLoop();

        LLVMBasicBlockRef head = labels.part1();
        LLVMBasicBlockRef end = labels.part2();
        LLVMBasicBlockRef l1 = v.getTranslation(n.body());

        LLVMPositionBuilderAtEnd(v.builder, v.currentBlock);
        LLVMBuildBr(v.builder, head);

        LLVMPositionBuilderAtEnd(v.builder, head);
        lang().translateLLVMConditional(n.cond(), v, l1, end);

        LLVMPositionBuilderAtEnd(v.builder, l1);
        LLVMBuildBr(v.builder, head);

        v.currentBlock = end;

        return super.translatePseudoLLVM(v);
    }
}
