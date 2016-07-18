package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class LLVMPointerType_c extends LLVMTypeNode_c
        implements LLVMPointerType {
    private static final long serialVersionUID = SerialVersionUID.generate();
    protected LLVMTypeNode pointerToType;

    public LLVMPointerType_c(Position pos, LLVMTypeNode tn, Ext e) {
        super(pos, e);
        pointerToType = tn;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(pointerToType, w, pp);
        w.write("*");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(pointerToType, v);
        return reconstruct(this, tn);
    }

    protected <N extends LLVMPointerType_c> N reconstruct(N n,
            LLVMTypeNode tn) {
        n = pointerToType(n, tn);
        return n;
    }

    protected <N extends LLVMPointerType_c> N pointerToType(N n,
            LLVMTypeNode tn) {
        if (n.pointerToType == tn) return n;
        n = copyIfNeeded(n);
        n.pointerToType = tn;
        return n;
    }

    @Override
    public LLVMTypeNode dereferenceType() {
        return pointerToType;
    }

}
