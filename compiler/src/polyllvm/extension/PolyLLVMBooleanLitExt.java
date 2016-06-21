package polyllvm.extension;

import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMBooleanLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        int value;
        if (n.value()) {
            value = 1;
        }
        else {
            value = 0;
        }

        v.addTranslation(node(),
                         nf.LLVMIntLiteral(Position.compilerGenerated(),
                                           value,
                                           null));
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        BooleanLit n = (BooleanLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        int value;
        if (n.value()) {
            value = 1;
        }
        else {
            value = 0;
        }
        return nf.LLVMBr(Position.compilerGenerated(),
                         nf.LLVMTypedOperand(Position.compilerGenerated(),
                                             nf.LLVMIntLiteral(Position.compilerGenerated(),
                                                               value,
                                                               null),
                                             nf.LLVMIntType(Position.compilerGenerated(),
                                                            1)),
                         trueLabel,
                         falseLabel);
    }
}
