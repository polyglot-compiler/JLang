package polyllvm.extension;

import polyglot.ast.Node;
import polyllvm.ast.Load;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMLoadExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Load n = (Load) node();
        LLVMValueRef ptr = v.getTranslation(n.expr());
        LLVMValueRef val = LLVMBuildLoad(v.builder, ptr, "load");
        v.addTranslation(n, val);
        return super.leaveTranslateLLVM(v);
    }
}
