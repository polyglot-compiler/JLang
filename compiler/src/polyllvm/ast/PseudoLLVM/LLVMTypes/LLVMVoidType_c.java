package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMVoidType_c extends LLVMTypeNode_c implements LLVMVoidType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMVoidType_c(Position pos) {
        super(pos, null);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(toString());
    }

    @Override
    public String toString() {
        return "void";
    }

}
