package polyllvm.ast.PseudoLLVM.Statements;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.RemoveESeqVisitor;

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
    public LLVMTypeNode retType() {
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

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMBinaryOperandInstruction_c n =
                (LLVMBinaryOperandInstruction_c) super.visitChildren(v);
        LLVMTypeNode tn = visitChild(typeNode, v);
        LLVMOperand l = visitChild(left, v);
        LLVMOperand r = visitChild(right, v);
        return reconstruct(n, tn, l, r);
    }

    protected <N extends LLVMBinaryOperandInstruction_c> N reconstruct(N n,
            LLVMTypeNode tn, LLVMOperand l, LLVMOperand r) {
        n = typeNode(n, tn);
        n = left(n, l);
        n = right(n, r);
        return n;
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (left instanceof LLVMESeq && right instanceof LLVMESeq) {
            LLVMESeq l = (LLVMESeq) left;
            LLVMESeq r = (LLVMESeq) right;
            //sl⃗ ; MOVE(TEMP(tl ), el′ ); s⃗r ; OP(TEMP(tr ), er′ )
            //sl ; Alloc tl; store el -> tl ; sr; Load tl -> freshtemp; OP(freshtemp, er')
            LLVMTypeNode ptrType = nf.LLVMPointerType(typeNode);
            LLVMVariable tempPointer =
                    PolyLLVMFreshGen.freshLocalVar(nf, ptrType);
            LLVMAlloca allocTemp = nf.LLVMAlloca(typeNode);
            allocTemp = allocTemp.result(tempPointer);
            LLVMStore moveLeft = nf.LLVMStore(typeNode, l.expr(), tempPointer);

            LLVMVariable freshTemp =
                    PolyLLVMFreshGen.freshLocalVar(nf, typeNode);
            LLVMLoad loadLeft = nf.LLVMLoad(freshTemp, typeNode, tempPointer);

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(l.instruction());
            instructions.add(allocTemp);
            instructions.add(moveLeft);
            instructions.add(r.instruction());
            instructions.add(loadLeft);
            instructions.add(reconstruct(this, typeNode, freshTemp, r.expr()));
            return nf.LLVMSeq(instructions);
        }
        else if (left instanceof LLVMESeq) {
            LLVMESeq l = (LLVMESeq) left;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(l.instruction());
            instructions.add(reconstruct(this, typeNode, l.expr(), right));
            return nf.LLVMSeq(instructions);
        }
        else if (right instanceof LLVMESeq) {
            LLVMESeq r = (LLVMESeq) right;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(r.instruction());
            instructions.add(reconstruct(this, typeNode, left, r.expr()));
            return nf.LLVMSeq(instructions);
        }
        else {
            return this;
        }
    }

}
