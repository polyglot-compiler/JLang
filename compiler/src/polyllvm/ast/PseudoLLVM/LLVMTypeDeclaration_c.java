package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMTypeDeclaration_c extends LLVMGlobalDeclaration_c
        implements LLVMTypeDeclaration {
    private static final long serialVersionUID = SerialVersionUID.generate();
    protected LLVMTypeNode typeNode;

    public LLVMTypeDeclaration_c(Position pos, LLVMTypeNode tn, Ext e) {
        super(pos, e);
        typeNode = tn;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(typeNode, w, pp);
        w.write(" = type opaque");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(typeNode, v);
        return reconstruct(this, tn);
    }

    protected <N extends LLVMTypeDeclaration_c> N reconstruct(N n,
            LLVMTypeNode tn) {
        n = typeNode(n, tn);
        return n;
    }

    protected <N extends LLVMTypeDeclaration_c> N typeNode(N n,
            LLVMTypeNode typeNode) {
        if (n.typeNode == typeNode) return n;
        n = copyIfNeeded(n);
        n.typeNode = typeNode;
        return n;
    }

}
