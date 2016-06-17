package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.ast.Node_c;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * @author Daniel
 *
 */
public abstract class LLVMNode_c extends Node_c implements LLVMNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMNode_c(Position pos, Ext e) {
        super(pos, e);
    }
}
