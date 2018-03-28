package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import polyglot.ast.For;
import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;

public class PolyLLVMForExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        For n = (For) node();

        LLVMBasicBlockRef cond = v.utils.buildBlock("for.cond");
        LLVMBasicBlockRef body = v.utils.buildBlock("for.body");
        LLVMBasicBlockRef end = v.utils.buildBlock("for.end");
        LLVMBasicBlockRef update = v.utils.buildBlock("for.update");

        v.pushLoop(update, end);
        if (parent instanceof Labeled) {
            // Override the head block defined by the label.
            // This ensures that the init statement is not re-run after a continue statement.
            String label = ((Labeled) parent).label();
            v.pushLabel(label, update, end);
        }

        // Initialization.
        n.visitList(n.inits(), v);
        v.utils.branchUnlessTerminated(cond);

        // Conditional (may be empty).
        LLVMPositionBuilderAtEnd(v.builder, cond);
        if (n.cond() != null) {
            lang().translateLLVMConditional(n.cond(), v, body, end);
        } else {
            LLVMBuildBr(v.builder, body);
        }

        // Update.
        LLVMPositionBuilderAtEnd(v.builder, update);
        n.visitList(n.iters(), v);
        v.utils.branchUnlessTerminated(cond);

        // Body.
        LLVMPositionBuilderAtEnd(v.builder, body);
        n.visitChild(n.body(), v);
        v.utils.branchUnlessTerminated(update);

        LLVMPositionBuilderAtEnd(v.builder, end);
        v.popLoop();
        return n;
    }
}
