package polyllvm.extension;

import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBooleanLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        v.debugInfo.emitLocation(n);
        LLVMValueRef val = LLVMConstInt(LLVMInt1Type(), n.value() ? 1 : 0, /* sign-extend */ 0);
        v.addTranslation(node(), val);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public void translateLLVMConditional(PseudoLLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        BooleanLit n = (BooleanLit) node();
        v.debugInfo.emitLocation(n);
        if (n.value()) {
            LLVMBuildBr(v.builder, trueBlock);
        } else {
            LLVMBuildBr(v.builder, falseBlock);
        }
    }
}
