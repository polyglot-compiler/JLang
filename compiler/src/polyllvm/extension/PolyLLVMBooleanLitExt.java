package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMBooleanLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        int value = n.value() ? 1 : 0;
        LLVMIntType type = nf.LLVMIntType(1);
        v.addTranslation(node(), nf.LLVMIntLiteral(type, value));
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
        if(((BooleanLit) node()).value()){
            LLVMBuildBr(v.builder, trueBlock);
        } else {
            LLVMBuildBr(v.builder, falseBlock);
        }
    }
}
