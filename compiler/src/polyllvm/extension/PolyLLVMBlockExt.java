package polyllvm.extension;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBlockExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        LLVMBasicBlockRef res = LLVMAppendBasicBlock(v.currFn(), "start");
        LLVMPositionBuilderAtEnd(v.builder, res);
        v.addTranslation(node(), res);
        v.currentBlock = res;
        return super.enterTranslatePseudoLLVM(v);
    }
}
