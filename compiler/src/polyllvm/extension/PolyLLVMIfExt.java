package polyllvm.extension;

import polyglot.ast.If;
import polyglot.ast.Node;
import polyglot.ast.Stmt;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.util.function.BiConsumer;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMIfExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        If n = (If) node();
        LLVMBasicBlockRef ifEnd = LLVMAppendBasicBlock(v.currFn(), "if_end");
        LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(v.currFn(), "if_true");
        LLVMBasicBlockRef ifFalse = n.alternative() != null
                ? LLVMAppendBasicBlock(v.currFn(), "if_false")
                : ifEnd;

        lang().translateLLVMConditional(n.cond(), v, ifTrue, ifFalse);

        BiConsumer<LLVMBasicBlockRef, Stmt> emitBlock = (block, stmt) -> {
            LLVMPositionBuilderAtEnd(v.builder, block);
            v.visitEdge(n, stmt);
            LLVMBasicBlockRef blockEnd = LLVMGetInsertBlock(v.builder);
            if (LLVMGetBasicBlockTerminator(blockEnd) == null) {
                LLVMBuildBr(v.builder, ifEnd);
            }
        };

        emitBlock.accept(ifTrue, n.consequent());
        if (n.alternative() != null) {
            emitBlock.accept(ifFalse, n.alternative());
        }

        LLVMPositionBuilderAtEnd(v.builder, ifEnd);
        return n;
    }
}
