package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import polyglot.ast.Do;
import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;

public class PolyLLVMDoExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Do n = (Do) node();

        LLVMBasicBlockRef body = v.utils.buildBlock("do.body");
        LLVMBasicBlockRef cond = v.utils.buildBlock("do.cond");
        LLVMBasicBlockRef end = v.utils.buildBlock("do.end");

        v.pushLoop(cond, end);
        if (parent instanceof Labeled) {
            // Override the head block defined by the label. This ensures that the body
            // of the do-while is not re-run after a continue statement.
            String label = ((Labeled) parent).label();
            v.pushLabel(label, cond, end);
        }

        // Body.
        LLVMBuildBr(v.builder, body);
        LLVMPositionBuilderAtEnd(v.builder, body);
        n.visitChild(n.body(), v);
        v.utils.branchUnlessTerminated(cond);

        // Conditional.
        LLVMPositionBuilderAtEnd(v.builder, cond);
        lang().translateLLVMConditional(n.cond(), v, body, end);

        LLVMPositionBuilderAtEnd(v.builder, end);
        v.popLoop();
        return n;
    }
}
