package jlang.extension;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.ast.JLangExt;
import jlang.types.JLangLocalInstance;
import jlang.visit.LLVMTranslator;

public class JLangLocalExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Local n = (Local) node();
        JLangLocalInstance li = (JLangLocalInstance) n.localInstance().orig();

        if (li.isSSA()) {
            // SSA values are already ready to use.
            v.addTranslation(n, v.getTranslation(li));
        }
        else {
            // Otherwise, we need to load from the stack.
            LLVMValueRef ptr = translateAsLValue(v);
            LLVMValueRef val = LLVMBuildLoad(v.builder, ptr, "load." + n.name());
            v.addTranslation(n, val);
        }

        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Local n = (Local) node();
        JLangLocalInstance li = (JLangLocalInstance) n.localInstance().orig();
        return v.getTranslation(li);
    }
}
