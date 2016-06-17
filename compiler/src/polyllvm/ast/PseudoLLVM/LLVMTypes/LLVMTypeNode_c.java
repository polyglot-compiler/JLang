package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.LLVMNode_c;

public abstract class LLVMTypeNode_c extends LLVMNode_c
        implements LLVMTypeNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMTypeNode_c(Position pos, Ext e) {
        super(pos, e);
    }

}
