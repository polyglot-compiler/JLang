package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public abstract class LLVMBinaryOperandInstruction_c extends LLVMInstruction_c
        implements LLVMBinaryOperandInstruction {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected LLVMOperand left;
    protected LLVMOperand right;

    public LLVMBinaryOperandInstruction_c(Position pos, LLVMVariable result,
            LLVMTypeNode tn, LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, e);
        this.result = result;
        typeNode = tn;
        this.left = left;
        this.right = right;
    }

    public LLVMBinaryOperandInstruction_c(Position pos, LLVMTypeNode tn,
            LLVMOperand left, LLVMOperand right, Ext e) {
        this(pos, null, tn, left, right, e);
    }

    @Override
    public LLVMVariable result() {
        return result;
    }

    @Override
    public LLVMBinaryOperandInstruction result(LLVMVariable o) {
        return (LLVMBinaryOperandInstruction) super.result(o);
    }

    @Override
    public LLVMBinaryOperandInstruction typeNode(LLVMTypeNode tn) {
        return typeNode(this, tn);
    }

    @Override
    public LLVMBinaryOperandInstruction left(LLVMOperand l) {
        return left(this, l);
    }

    @Override
    public LLVMBinaryOperandInstruction right(LLVMOperand r) {
        return right(this, r);
    }

    protected <N extends LLVMBinaryOperandInstruction_c> N typeNode(N n,
            LLVMTypeNode typeNode) {
        if (n.typeNode == typeNode) return n;
        n = copyIfNeeded(n);
        n.typeNode = typeNode;
        return n;
    }

    protected <N extends LLVMBinaryOperandInstruction_c> N left(N n,
            LLVMOperand left) {
        if (n.left == left) return n;
        n = copyIfNeeded(n);
        n.left = left;
        return n;
    }

    protected <N extends LLVMBinaryOperandInstruction_c> N right(N n,
            LLVMOperand right) {
        if (n.right == right) return n;
        n = copyIfNeeded(n);
        n.right = right;
        return n;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

    @Override
    public LLVMOperand left() {
        return left;
    }

    @Override
    public LLVMOperand right() {
        return right;
    }

}
