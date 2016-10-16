package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.RemoveESeqVisitor;

import java.util.ArrayList;
import java.util.List;

public class LLVMStore_c extends LLVMInstruction_c implements LLVMStore {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected LLVMOperand value;
    protected LLVMOperand ptr;

    public LLVMStore_c(Position pos, LLVMTypeNode typeNode, LLVMOperand value,
            LLVMOperand ptr, Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
        this.value = value;
        this.ptr = ptr;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        super.prettyPrint(w, pp);
        w.write("store ");
        print(typeNode, w, pp);
        w.write(" ");
        print(value, w, pp);
        w.write(", ");
        print(typeNode, w, pp);
        w.write("* ");
        print(ptr, w, pp);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMStore_c n = (LLVMStore_c) super.visitChildren(v);
        LLVMTypeNode tn = visitChild(typeNode, v);
        LLVMOperand val = visitChild(value, v);
        LLVMOperand p = visitChild(ptr, v);

        return reconstruct(n, tn, val, p);
    }

    protected <N extends LLVMStore_c> N reconstruct(N n, LLVMTypeNode tn,
            LLVMOperand val, LLVMOperand p) {
        n = typeNode(n, tn);
        n = value(n, val);
        n = ptr(n, p);
        return n;
    }

    protected <N extends LLVMStore_c> N typeNode(N n, LLVMTypeNode tn) {
        if (n.typeNode == tn) return n;
        n = copyIfNeeded(n);
        n.typeNode = tn;
        return n;
    }

    protected <N extends LLVMStore_c> N value(N n, LLVMOperand val) {
        if (n.value == val) return n;
        n = copyIfNeeded(n);
        n.value = val;
        return n;
    }

    protected <N extends LLVMStore_c> N ptr(N n, LLVMOperand p) {
        if (n.ptr == p) return n;
        n = copyIfNeeded(n);
        n.ptr = p;
        return n;
    }

    @Override
    public LLVMTypeNode retType() {
        return typeNode;
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        if (value instanceof LLVMESeq && ptr instanceof LLVMESeq) {
            //TODO: Might need to make this case more complicated!
            PolyLLVMNodeFactory nf = v.nodeFactory();

            LLVMESeq valueESeq = (LLVMESeq) value;
            LLVMESeq ptrESeq = (LLVMESeq) ptr;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(valueESeq.instruction());
            instructions.add(ptrESeq.instruction());
            instructions.add(reconstruct(this,
                                         typeNode,
                                         valueESeq.expr(),
                                         ptrESeq.expr()));
            LLVMSeq llvmSeq =
                    nf.LLVMSeq(instructions);

            return llvmSeq;

        }
        else if (value instanceof LLVMESeq) {
            PolyLLVMNodeFactory nf = v.nodeFactory();

            LLVMESeq eseq = (LLVMESeq) value;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(eseq.instruction());
            instructions.add(reconstruct(this, typeNode, eseq.expr(), ptr));
            LLVMSeq llvmSeq =
                    nf.LLVMSeq(instructions);

            return llvmSeq;
        }
        else if (ptr instanceof LLVMESeq) {
            PolyLLVMNodeFactory nf = v.nodeFactory();

            LLVMESeq eseq = (LLVMESeq) ptr;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(eseq.instruction());
            instructions.add(reconstruct(this, typeNode, value, eseq.expr()));
            LLVMSeq llvmSeq =
                    nf.LLVMSeq(instructions);

            return llvmSeq;

        }

        return this;
    }

}
