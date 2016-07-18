package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public abstract class LLVMGlobalDeclaration_c extends LLVMNode_c
        implements LLVMGlobalDeclaration {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMGlobalDeclaration_c(Position pos, Ext e) {
        super(pos, e);
    }

}
