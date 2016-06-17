package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;

public class AddVoidReturnVisitor extends NodeVisitor {

    private PolyLLVMNodeFactory nf;
    private TypeSystem ts;

    public AddVoidReturnVisitor(TypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;

    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().addVoidReturn(n, this);
    }

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    @Override
    public String toString() {
        return "AddVoidReturn";
    }

}
