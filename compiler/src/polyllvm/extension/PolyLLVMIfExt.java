package polyllvm.extension;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.If;
import polyglot.ast.Node;
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

import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlock;

public class PolyLLVMIfExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        If n = (If) node();

        LLVMBasicBlockRef entry = v.currentBlock;
        LLVMBasicBlockRef iftrue = v.getTranslation(n.consequent());
        LLVMBasicBlockRef iffalse = n.alternative() == null
                ? LLVMAppendBasicBlock(v.currFn(), "if_false")
                : v.getTranslation(n.alternative());
        LLVMBasicBlockRef end = LLVMAppendBasicBlock(v.currFn(), "end");

        LLVMPositionBuilderAtEnd(v.builder, entry);

        lang().translateLLVMConditional(n.cond(), v, iftrue, iffalse);

        LLVMPositionBuilderAtEnd(v.builder, iftrue);
        LLVMBuildBr(v.builder, end);

        LLVMPositionBuilderAtEnd(v.builder, iffalse);
        LLVMBuildBr(v.builder, end);

        LLVMPositionBuilderAtEnd(v.builder, end);
        v.currentBlock = end;

        return super.translatePseudoLLVM(v);
    }

}
