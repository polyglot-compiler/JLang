package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

//TODO: check if need ESEQ function
public class LLVMTypedOperand_c extends LLVMOperand_c
        implements LLVMTypedOperand {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected LLVMOperand operand;

    public LLVMTypedOperand_c(Position pos, LLVMOperand op, LLVMTypeNode tn,
            Ext e) {
        super(pos, e);
        typeNode = tn;
        operand = op;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(typeNode, w, pp);
        w.write(" ");
        print(operand, w, pp);
    }

    @Override
    public String toString() {
        return typeNode.toString() + " " + operand.toString();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(typeNode, v);
        LLVMOperand o = visitChild(operand, v);
        return reconstruct(this, tn, o);
    }

    protected <N extends LLVMTypedOperand_c> N reconstruct(N n, LLVMTypeNode tn,
            LLVMOperand o) {
        n = typeNode(n, tn);
        n = operand(n, o);
        return n;
    }

    protected <N extends LLVMTypedOperand_c> N typeNode(N n, LLVMTypeNode tn) {
        if (n.typeNode == tn) return n;
        n = copyIfNeeded(n);
        n.typeNode = tn;
        return n;
    }

    protected <N extends LLVMTypedOperand_c> N operand(N n, LLVMOperand o) {
        if (n.operand == o) return n;
        n = copyIfNeeded(n);
        n.operand = o;
        return n;
    }

    @Override
    public LLVMOperand operand() {
        return operand;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

}
