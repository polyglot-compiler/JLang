package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMIntType_c extends LLVMTypeNode_c implements LLVMIntType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private int intSize;

    public LLVMIntType_c(Position pos, int intSize, Ext e) {
        super(pos, e);
        this.intSize = intSize;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(toString());
    }

    @Override
    public String toString() {
        return "i" + intSize;
    }

}
