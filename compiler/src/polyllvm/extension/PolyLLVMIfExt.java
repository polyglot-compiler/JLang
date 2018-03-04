package polyllvm.extension;

import polyglot.ast.If;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMIfExt extends PolyLLVMExt {
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
        n.consequent().visit(v);
        v.utils.branchUnlessTerminated(ifEnd);

        if (n.alternative() != null) {
            LLVMPositionBuilderAtEnd(v.builder, ifFalse);
            n.alternative().visit(v);
            v.utils.branchUnlessTerminated(ifEnd);
        }

        LLVMPositionBuilderAtEnd(v.builder, ifEnd);
        return n;
    }
}
