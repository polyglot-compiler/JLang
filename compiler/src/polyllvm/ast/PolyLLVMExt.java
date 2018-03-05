package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.LLVMTranslator;
import polyllvm.visit.DesugarLocally;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMExt extends Ext_c implements PolyLLVMOps {
    private static final long serialVersionUID = SerialVersionUID.generate();


    public static PolyLLVMExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof PolyLLVMExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No PolyLLVM extension object for node "
                                            + n + " (" + n.getClass() + ")", n.position());
        }
        return (PolyLLVMExt) e;
    }

    @Override
    public final PolyLLVMLang lang() {
        return PolyLLVMLang_c.instance;
    }

    @Override
    public Node desugar(DesugarLocally v) {
        return node();
    }

    @Override
    public LLVMTranslator enterTranslateLLVM(LLVMTranslator v) {
        return v;
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        return node();
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        return null;
    }

    @Override
    public void translateLLVMConditional(LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        Node n = v.visitEdge(null, node());
        LLVMBuildCondBr(v.builder, v.getTranslation(n), trueBlock, falseBlock);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        throw new InternalCompilerError("Unable to translate to lvalue: " + node());
    }

    /** Helper method for copying nodes. Recall that nodes must not alias each other in the AST. */
    @SuppressWarnings("unchecked")
    protected <T> T copy(Node n) {
        return (T) n.copy();
    }
}
