package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;

public class PrintVisitor extends ContextVisitor {

    public PrintVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        return lang().print(n, this);
    }

}
