package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMLabel_c extends LLVMExpr_c implements LLVMLabel {
    private static final long serialVersionUID = SerialVersionUID.generate();
    private String name;

    public LLVMLabel_c(Position pos, String name, Ext e) {
        super(pos, e);
        this.name = name;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("%" + name);
    }

    @Override
    public String name() {
        return name;
    }

}
