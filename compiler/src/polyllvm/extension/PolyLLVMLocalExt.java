package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Local n = (Local) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
//        LLVMVariable translation =
//                nf.LLVMVariable(Position.compilerGenerated(),
//                                n.name(),
//                                VarType.LOCAL);
        LLVMTypeNode typeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());

        LLVMVariable expr = PolyLLVMFreshGen.freshLocalVar(nf, typeNode);

        LLVMVariable ptr =
                nf.LLVMVariable(Position.compilerGenerated(),
                                v.varName(n.name()),
                                typeNode,
                                VarType.LOCAL);
        LLVMInstruction instruction =
                nf.LLVMLoad(expr, typeNode, ptr);
        LLVMNode translation =
                nf.LLVMESeq(instruction, expr);
        v.addTranslation(n, translation);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypedOperand typedTranslation =
                nf.LLVMTypedOperand((LLVMOperand) v.getTranslation(node()),
                                    nf.LLVMIntType(1));
        LLVMInstruction translation =
                nf.LLVMBr(Position.compilerGenerated(),
                          typedTranslation,
                          trueLabel,
                          falseLabel);

        return translation;
    }
}
