package jlang.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangExt extends Ext_c implements JLangOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JLangExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JLangExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JLang extension object for node "
                                            + n + " (" + n.getClass() + ")", n.position());
        }
        return (JLangExt) e;
    }

    @Override
    public final JLangLang lang() {
        return JLangLang_c.instance;
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
