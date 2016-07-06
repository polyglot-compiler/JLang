package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMPointerType_c extends LLVMTypeNode_c
        implements LLVMPointerType {
    private static final long serialVersionUID = SerialVersionUID.generate();
    protected LLVMTypeNode pointerToType;

    public LLVMPointerType_c(Position pos, LLVMTypeNode tn, Ext e) {
        super(pos, e);
        pointerToType = tn;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(pointerToType, w, pp);
        w.write("*");
    }

}
