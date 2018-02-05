package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Do;
import polyglot.ast.For;
import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;

public class PolyLLVMLabeledExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Labeled n = (Labeled) node();

        if (n instanceof For || n instanceof Do) {
            throw new InternalCompilerError("The translation for labels assumes that " +
                    "for-loops and do-while-loops have been normalized to while-loops, " +
                    "since they could have initialization code that should not be run " +
                    "after reaching a continue statement jumping to this label");
        }

        LLVM.LLVMBasicBlockRef head = LLVMAppendBasicBlockInContext(
                v.context, v.currFn(), n.label() + ".head");
        LLVM.LLVMBasicBlockRef end = LLVMAppendBasicBlockInContext(
                v.context, v.currFn(), n.label() + ".end");

        LLVMBuildBr(v.builder, head);
        LLVMPositionBuilderAtEnd(v.builder, head);

        v.pushLabel(n.label(), head, end);
        n.visitChild(n.statement(), v);
        v.popLabel(n.label());

        v.utils.branchUnlessTerminated(end);
        LLVMPositionBuilderAtEnd(v.builder, end);

        return n;
    }
}
