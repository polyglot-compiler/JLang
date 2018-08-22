package jlang.extension;

import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.*;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangIntLitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        IntLit n = (IntLit) node();
        assert n.type().isLongOrLess();
        LLVMTypeRef type = v.utils.toLL(n.type());
        LLVMValueRef res = LLVMConstInt(type, n.value(), /*sign-extend*/ 0);
        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }
}
