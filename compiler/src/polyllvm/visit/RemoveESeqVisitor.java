package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;

public class RemoveESeqVisitor extends NodeVisitor {

    private PolyLLVMNodeFactory nf;

    public RemoveESeqVisitor(PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
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

}
