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
    public Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        While n = (While) node();
        v.enterLoop(n);

        Pair<LLVMBasicBlockRef, LLVMBasicBlockRef> labels = v.peekLoop();

        LLVMBasicBlockRef head = labels.part1();
        LLVMBasicBlockRef end = labels.part2();
        LLVMBasicBlockRef l1 = LLVMAppendBasicBlock(v.currFn(), "l1");

        LLVMPositionBuilderAtEnd(v.builder, LLVMGetInsertBlock(v.builder));
        LLVMBuildBr(v.builder, head);

        LLVMPositionBuilderAtEnd(v.builder, head);
        v.visitEdge(n, n.cond());
        lang().translateLLVMConditional(n.cond(), v, l1, end);

        LLVMPositionBuilderAtEnd(v.builder, l1);
        v.visitEdge(n, n.body());
        LLVMBasicBlockRef blockEnd = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(blockEnd) == null) {
            LLVMBuildBr(v.builder, head);
        }
        LLVMPositionBuilderAtEnd(v.builder, end);
        v.leaveLoop();
        return n;
    }
}
