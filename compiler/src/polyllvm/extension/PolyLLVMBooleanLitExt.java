package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBooleanLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        LLVMValueRef val = LLVMConstInt(LLVMInt1Type(), n.value() ? 1 : 0 ,0);
        v.addTranslation(node(), val);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        BooleanLit n = (BooleanLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (n.value()) {
            return nf.LLVMBr(trueLabel);
        }
        else {
            return nf.LLVMBr(falseLabel);
        }
    }

    @Override
    public void translateLLVMConditional(PseudoLLVMTranslator v, LLVM.LLVMBasicBlockRef trueBlock, LLVM.LLVMBasicBlockRef falseBlock) {
        if (((BooleanLit) node()).value()) {
            LLVMBuildBr(v.builder, trueBlock);
        } else {
            LLVMBuildBr(v.builder, falseBlock);
        }
    }
}
