package jlang.extension;

import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.*;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangSpecialExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Special n = (Special) node();

        if (n.qualifier() != null)
            throw new InternalCompilerError(
                    "Qualified this should have been desugared by the DesugarInnerClasses visitor");
        if (n.kind().equals(Special.SUPER))
            throw new InternalCompilerError(
                    "super should have been desugared by the DesugarInnerClasses visitor");
        assert n.kind().equals(Special.THIS);

        v.addTranslation(n, LLVMGetParam(v.currFn(), 0));
        return super.leaveTranslateLLVM(v);
    }
}
