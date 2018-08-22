package jlang.extension;

import org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMPositionBuilderAtEnd;

public class JLangLabeledExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Labeled n = (Labeled) node();

        // Any statement can be labeled. We need to make sure that the translation of a labeled
        // statement starts and ends at basic block boundaries so that break and continue statements
        // work as expected. (This may only be relevant for Java blocks.)
        //
        // Note that some loop translations override the head block so that continue
        // statements jump to the correct part of the loop, skipping initialization statements.
        LLVMBasicBlockRef head = v.utils.buildBlock(n.label() + ".head");
        LLVMBasicBlockRef end = v.utils.buildBlock(n.label() + ".end");

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
