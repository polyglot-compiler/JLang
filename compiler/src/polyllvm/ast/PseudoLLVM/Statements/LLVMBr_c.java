package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;

public class LLVMBr_c extends LLVMInstruction_c implements LLVMBr {
    /*
     * Invariant: If cond, trueLabel, and falseLabel are all non-null, then this
     * is unconditional br instruction, else only trueLabel may be non-null.
     */
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypedOperand cond;
    protected LLVMLabel trueLabel;
    protected LLVMLabel falseLabel;

    public LLVMBr_c(Position pos, LLVMTypedOperand cond, LLVMLabel trueLabel,
            LLVMLabel falseLabel, Ext e) {
        super(pos, e);
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public LLVMBr_c(Position pos, LLVMLabel l, Ext e) {
        super(pos, e);
        trueLabel = l;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("br ");

        if (cond == null && falseLabel == null) {
            w.write("label ");
            print(trueLabel, w, pp);
        }
        else {
            print(cond, w, pp);
            w.write(", label ");
            print(trueLabel, w, pp);
            w.write(", label ");
            print(falseLabel, w, pp);
        }

    }

}
