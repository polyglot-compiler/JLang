package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public abstract class LLVMCmp_c extends LLVMBinaryOperandInstruction_c
        implements LLVMCmp {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected ConditionCode conditionCode;

    public LLVMCmp_c(Position pos, LLVMVariable result, ConditionCode cc,
            LLVMTypeNode tn, LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, result, tn, left, right, e);
        conditionCode = cc;

    }

    public LLVMCmp_c(Position pos, ConditionCode cc, LLVMTypeNode tn,
            LLVMOperand left, LLVMOperand right, Ext e) {
        this(pos, null, cc, tn, left, right, e);
    }

}
