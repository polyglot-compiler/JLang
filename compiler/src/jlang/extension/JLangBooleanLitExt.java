//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.*;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangBooleanLitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        LLVMTypeRef type = v.utils.toLL(n.type());
        LLVMValueRef val = LLVMConstInt(type, n.value() ? 1 : 0, /*sign-extend*/ 0);
        v.addTranslation(node(), val);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public void translateLLVMConditional(LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        BooleanLit n = (BooleanLit) node();
        LLVMBuildBr(v.builder, n.value() ? trueBlock : falseBlock);
    }
}
