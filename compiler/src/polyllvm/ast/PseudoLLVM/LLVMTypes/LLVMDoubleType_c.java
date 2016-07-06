package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMDoubleType_c extends LLVMTypeNode_c implements LLVMDoubleType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMDoubleType_c(Position pos, Ext e) {
        super(pos, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("double");
    }

}
