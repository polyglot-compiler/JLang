package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class LLVMArrayType_c extends LLVMTypeNode_c implements LLVMArrayType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode arrayType;
    protected int length;

    public LLVMArrayType_c(Position pos, LLVMTypeNode arrayType, int length,
            Ext e) {
        super(pos, e);
        this.arrayType = arrayType;
        this.length = length;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("[" + length + " x ");
        print(arrayType, w, pp);
        w.write("]");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(arrayType, v);
        return reconstruct(this, tn);
    }

    protected <N extends LLVMArrayType_c> N reconstruct(N n, LLVMTypeNode tn) {
        n = arrayType(n, tn);
        return n;
    }

    protected <N extends LLVMArrayType_c> N arrayType(N n, LLVMTypeNode tn) {
        if (n.arrayType == tn) return n;
        n = copyIfNeeded(n);
        n.arrayType = tn;
        return n;
    }

}
