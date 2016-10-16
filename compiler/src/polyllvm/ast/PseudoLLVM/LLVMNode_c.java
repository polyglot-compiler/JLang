package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyllvm.visit.RemoveESeqVisitor;

import java.io.ByteArrayOutputStream;

/**
 * @author Daniel
 *
 */
public abstract class LLVMNode_c extends Node_c implements LLVMNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMNode_c(Position pos, Ext e) {
        super(pos, e);
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return super.visitChildren(v);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        prettyPrint(lang(), baos);
        return baos.toString();
    }
}
