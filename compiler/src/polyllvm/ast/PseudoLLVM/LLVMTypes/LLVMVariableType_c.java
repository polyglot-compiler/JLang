package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMVariableType_c extends LLVMTypeNode_c
        implements LLVMVariableType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;

    public LLVMVariableType_c(Position pos, String name, Ext e) {
        super(pos, e);
        this.name = name;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("%");
        w.write(name);
    }
}
