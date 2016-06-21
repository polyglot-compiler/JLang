package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.LLVMNode_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;

public abstract class LLVMInstruction_c extends LLVMNode_c
        implements LLVMInstruction {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMVariable result;

    public LLVMInstruction_c(Position pos, Ext e) {
        super(pos, e);
    }

    @Override
    public LLVMInstruction result(LLVMVariable o) {
        return result(this, o);
    }

    protected <N extends LLVMInstruction_c> N result(N n, LLVMVariable result) {
        if (n.result == result) return n;
        n = copyIfNeeded(n);
        n.result = result;
        return n;
    }

    @Override
    public LLVMVariable result() {
        return result;
    }
}
