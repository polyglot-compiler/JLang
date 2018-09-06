//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.LLVMGetParam;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.visit.LLVMTranslator;

public class JLangConstructorCallExt extends JLangProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        if (node().qualifier() != null)
            throw new InternalCompilerError("Qualified ctor call should have been desugared");

        // Most of the translation happens in this super call.
        return super.leaveTranslateLLVM(v);
    }

    @Override
    protected LLVMValueRef buildReceiverArg(LLVMTranslator v) {
        return LLVMGetParam(v.currFn(), 0);
    }

    @Override
    public ConstructorCall node() {
        return (ConstructorCall) super.node();
    }
}
