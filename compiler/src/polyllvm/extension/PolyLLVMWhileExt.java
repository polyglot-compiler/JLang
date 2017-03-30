package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.While;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMWhileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        While n = (While) node();

        LLVMBasicBlockRef head = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "head");
        LLVMBasicBlockRef end = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "end");
        LLVMBasicBlockRef body = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "body");
        v.pushLoop(head, end);

        v.debugInfo.emitLocation(n);

        LLVMPositionBuilderAtEnd(v.builder, LLVMGetInsertBlock(v.builder));
        LLVMBuildBr(v.builder, head);

        LLVMPositionBuilderAtEnd(v.builder, head);
        lang().translateLLVMConditional(n.cond(), v, body, end);

        LLVMPositionBuilderAtEnd(v.builder, body);
        v.visitEdge(n, n.body());
        LLVMBasicBlockRef blockEnd = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(blockEnd) == null) {
            LLVMBuildBr(v.builder, head);
        }
        LLVMPositionBuilderAtEnd(v.builder, end);

        v.popLoop();
        return n;
    }
}
