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
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        While n = (While) node();

        LLVMBasicBlockRef head = v.utils.buildBlock("head");
        LLVMBasicBlockRef body = v.utils.buildBlock("body");
        LLVMBasicBlockRef end = v.utils.buildBlock("end");
        v.pushLoop(head, end);

        LLVMBuildBr(v.builder, head);
        LLVMPositionBuilderAtEnd(v.builder, head);
        lang().translateLLVMConditional(n.cond(), v, body, end);

        LLVMPositionBuilderAtEnd(v.builder, body);
        v.visitEdge(n, n.body());
        v.utils.branchUnlessTerminated(head);
        LLVMPositionBuilderAtEnd(v.builder, end);

        v.popLoop();
        return n;
    }
}
