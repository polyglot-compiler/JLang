package jlang.extension;

import polyglot.ast.If;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangIfExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        If n = (If) node();
        LLVMBasicBlockRef ifEnd = v.utils.buildBlock("if.end");
        LLVMBasicBlockRef ifTrue = v.utils.buildBlock("if.true");
        LLVMBasicBlockRef ifFalse = n.alternative() != null
                ? v.utils.buildBlock("if.false")
                : ifEnd;

        lang().translateLLVMConditional(n.cond(), v, ifTrue, ifFalse);

        LLVMPositionBuilderAtEnd(v.builder, ifTrue);
        n.visitChild(n.consequent(), v);
        v.utils.branchUnlessTerminated(ifEnd);

        if (n.alternative() != null) {
            LLVMPositionBuilderAtEnd(v.builder, ifFalse);
            n.visitChild(n.alternative(), v);
            v.utils.branchUnlessTerminated(ifEnd);
        }

        LLVMPositionBuilderAtEnd(v.builder, ifEnd);
        return n;
    }
}
