package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.visit.RemoveESeqVisitor;

import java.util.ArrayList;
import java.util.List;

public class LLVMESeq_c extends LLVMOperand_c implements LLVMESeq {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMInstruction instruction;
    protected LLVMOperand expr;

    public LLVMESeq_c(Position pos, LLVMInstruction instruction,
            LLVMOperand expr, Ext e) {
        super(pos, e);
        this.instruction = instruction;
        this.expr = expr;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("ESEQ(");
        print(instruction, w, pp);
        w.write(", ");
        print(expr, w, pp);
        w.write(")");
    }

    @Override
    public LLVMInstruction instruction() {
        return instruction;
    }

    @Override
    public LLVMOperand expr() {
        return expr;
    }

    //this(s1, ESEQ(s2, e)) --> ESEQ(SEQ(s1,s2), e)

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (expr instanceof LLVMESeq) {
            LLVMESeq eseq = (LLVMESeq) expr;
            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(instruction());
            instructions.add(eseq.instruction());
            LLVMSeq seq = nf.LLVMSeq(instructions);
            return eseq.instruction(seq);
        }
        return this;
    }

    @Override
    public LLVMESeq instruction(LLVMInstruction i) {
        return instruction(this, i);
    }

    protected <N extends LLVMESeq_c> N instruction(N n, LLVMInstruction i) {
        if (n.instruction == i) return n;
        n = copyIfNeeded(n);
        n.instruction = i;
        return n;
    }

    @Override
    public LLVMESeq expr(LLVMOperand e) {
        return expr(this, e);
    }

    protected <N extends LLVMESeq_c> N expr(N n, LLVMOperand e) {
        if (n.expr == e) return n;
        n = copyIfNeeded(n);
        n.expr = e;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMInstruction i = visitChild(instruction, v);
        LLVMOperand o = visitChild(expr, v);
        return reconstruct(this, i, o);
    }

    protected <N extends LLVMESeq_c> N reconstruct(N n, LLVMInstruction i,
            LLVMOperand o) {
        n = instruction(n, i);
        n = expr(n, o);
        return n;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return expr.typeNode();
    }

}
