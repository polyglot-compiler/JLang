package polyllvm.visit;

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;

public class RemoveESeqVisitor extends NodeVisitor {

    private PolyLLVMNodeFactory nf;
    private Set<LLVMESeq> visited;

    public RemoveESeqVisitor(PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
        visited = new HashSet<>();
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().removeESeq(n, this);
    }

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    /**
     * Returns true if the LLVMESeq {@code node} has already been visited
     */
    public boolean isVisited(LLVMESeq node) {
        return visited.contains(node);
    }

    /**
     * Set the LLVMESeq {@code node} as visited by this visitor
     */
    public void setVisited(LLVMESeq node) {
        visited.add(node);
    }

}
