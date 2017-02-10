package polyllvm.extension;

import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBlockExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(v.currFn(), "block");
        LLVMPositionBuilderAtEnd(v.builder, block);
        v.addTranslation(node(), block);
        v.currentBlock = block;
        return super.enterTranslatePseudoLLVM(v);
    }
}
