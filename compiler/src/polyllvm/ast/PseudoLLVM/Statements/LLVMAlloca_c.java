package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMAlloca_c extends LLVMInstruction_c implements LLVMAlloca {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected int numElements;
    protected int alignment;

    public LLVMAlloca_c(Position pos, LLVMTypeNode typeNode, int numElements,
            int alignment, Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
        this.numElements = numElements;
        this.alignment = alignment;
    }

    public LLVMAlloca_c(Position pos, LLVMTypeNode typeNode, int numElements,
            Ext e) {
        this(pos, typeNode, numElements, 0, e);
    }

    public LLVMAlloca_c(Position pos, LLVMTypeNode typeNode, Ext e) {
        this(pos, typeNode, 1, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result != null) {
            print(result(), w, pp);
            w.write(" = ");
        }
        w.write("alloca ");
        print(typeNode, w, pp);
        w.write(", i32");
        //print(typeNode, w, pp);
        w.write(" ");
        w.write(Integer.toString(numElements));
        if (alignment != 0) {
            w.write(", align ");
            w.write(Integer.toString(alignment));
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (result != null) {
            b.append(result());
            b.append(" = ");
        }
        b.append("alloca ");
        b.append(typeNode);
        b.append(", ");
        b.append(typeNode);
        b.append(" ");
        b.append(Integer.toString(numElements));
        if (alignment != 0) {
            b.append(", align ");
            b.append(Integer.toString(alignment));
        }
        return b.toString();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMAlloca_c n = (LLVMAlloca_c) super.visitChildren(v);
        LLVMTypeNode tn = visitChild(typeNode, v);
        return reconstruct(n, tn);
    }

    protected <N extends LLVMAlloca_c> N reconstruct(N n, LLVMTypeNode tn) {
        n = typeNode(n, tn);
        return n;
    }

    protected <N extends LLVMAlloca_c> N typeNode(N n, LLVMTypeNode tn) {
        if (n.typeNode == tn) return n;
        n = copyIfNeeded(n);
        n.typeNode = tn;
        return n;
    }

    @Override
    public LLVMTypeNode retType() {
        return typeNode;
    }

    @Override
    public LLVMAlloca result(LLVMVariable o) {
        // TODO Auto-generated method stub
        return (LLVMAlloca) super.result(o);
    }

}
