package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Local n = (Local) node();
        v.addTranslation(n, LLVMBuildLoad(v.builder, v.getVariable(n.name()), "load_" + n.name()));
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypedOperand typedTranslation =
                nf.LLVMTypedOperand(v.getTranslation(node()),
                                    nf.LLVMIntType(1));
        LLVMInstruction translation =
                nf.LLVMBr(typedTranslation, trueLabel, falseLabel);

        return translation;
    }

    @Override
    public void translateLLVMConditional(PseudoLLVMTranslator v, LLVM.LLVMBasicBlockRef trueBlock, LLVM.LLVMBasicBlockRef falseBlock) {
        LLVMBuildCondBr(v.builder, v.getTranslation(node()), trueBlock, falseBlock);
    }
}
