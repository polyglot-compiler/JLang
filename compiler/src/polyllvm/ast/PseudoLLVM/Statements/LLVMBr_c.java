package polyllvm.ast.PseudoLLVM.Statements;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.RemoveESeqVisitor;

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

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMBr_c n = (LLVMBr_c) super.visitChildren(v);
        LLVMTypedOperand cnd = visitChild(cond, v);
        LLVMLabel t = visitChild(trueLabel, v);
        LLVMLabel f = visitChild(falseLabel, v);
        return reconstruct(n, cnd, t, f);
    }

    protected <N extends LLVMBr_c> N reconstruct(N n, LLVMTypedOperand cnd,
            LLVMLabel t, LLVMLabel f) {
        n = cond(n, cnd);
        n = trueLabel(n, t);
        n = falseLabel(n, f);
        return n;
    }

    protected <N extends LLVMBr_c> N cond(N n, LLVMTypedOperand cnd) {
        if (n.cond == cnd) return n;
        n = copyIfNeeded(n);
        n.cond = cnd;
        return n;
    }

    protected <N extends LLVMBr_c> N trueLabel(N n, LLVMLabel t) {
        if (n.trueLabel == t) return n;
        n = copyIfNeeded(n);
        n.trueLabel = t;
        return n;
    }

    protected <N extends LLVMBr_c> N falseLabel(N n, LLVMLabel f) {
        if (n.falseLabel == f) return n;
        n = copyIfNeeded(n);
        n.falseLabel = f;
        return n;
    }

    @Override
    public LLVMTypeNode retType() {
        throw new InternalCompilerError("LLVM instruction br does not have a type");
    }

    @Override
    public LLVMTypedOperand cond() {
        return cond;
    }

    @Override
    public LLVMBr cond(LLVMTypedOperand temp) {
        return cond(this, temp);
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        if (cond != null) {
            PolyLLVMNodeFactory nf = v.nodeFactory();
            LLVMOperand untypedOp = cond.operand();
            if (untypedOp instanceof LLVMESeq) {
                LLVMESeq e = (LLVMESeq) untypedOp;
                LLVMTypedOperand llvmTypedOperand =
                        nf.LLVMTypedOperand(e.expr(),
                                            cond.typeNode());

                List<LLVMInstruction> instructions = new ArrayList<>();
                instructions.add(e.instruction());
                instructions.add(reconstruct(this,
                                             llvmTypedOperand,
                                             trueLabel,
                                             falseLabel));
                return nf.LLVMSeq(Position.compilerGenerated(), instructions);
            }
        }
        return this;
    }
}
