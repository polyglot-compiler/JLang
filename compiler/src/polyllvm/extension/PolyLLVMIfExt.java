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
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        If n = (If) node();
        LLVMBasicBlockRef ifEnd = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "if_end");
        LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "if_true");
        LLVMBasicBlockRef ifFalse = n.alternative() != null
                ? LLVMAppendBasicBlockInContext(v.context, v.currFn(), "if_false")
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


    // Helper function used by other translations.
    static void buildIf(LLVMTranslator v, LLVMValueRef cond, Runnable builder) {
        LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "if_true");
        LLVMBasicBlockRef ifEnd = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "if_end");
        LLVMBuildCondBr(v.builder, cond, ifTrue, ifEnd);
        LLVMPositionBuilderAtEnd(v.builder, ifTrue);
        builder.run();
        v.utils.branchUnlessTerminated(ifEnd);
        LLVMPositionBuilderAtEnd(v.builder, ifEnd);
    }
}
