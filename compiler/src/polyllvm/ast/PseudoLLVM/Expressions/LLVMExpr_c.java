package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.LLVMNode_c;

public class LLVMExpr_c extends LLVMNode_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMExpr_c(Position pos, Ext e) {
        super(pos, e);
    }

}
