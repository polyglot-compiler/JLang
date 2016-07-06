package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMArgDecl_c extends LLVMNode_c implements LLVMArgDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected String name;

    public LLVMArgDecl_c(Position pos, LLVMTypeNode typeNode, String name,
            Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
        this.name = name;
    }

    public LLVMArgDecl_c(Position pos, LLVMTypeNode typeNode, String name) {
        this(pos, typeNode, name, null);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(typeNode, w, pp);
        w.write(" %");
        w.write(name);
    }

    @Override
    public String toString() {
        return typeNode + " %" + name;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(typeNode, v);
        return reconstruct(this, tn);
    }

    /** Reconstruct the LLVM ArgDecl. */
    protected <N extends LLVMArgDecl_c> N reconstruct(N n, LLVMTypeNode tn) {
        n = typeNode(n, tn);
        return n;
    }

    protected <N extends LLVMArgDecl_c> N typeNode(N n, LLVMTypeNode tn) {
        if (n.typeNode == tn) return n;
        n = copyIfNeeded(n);
        n.typeNode = tn;
        return n;
    }

    @Override
    public String varName() {
        return name;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }
}
