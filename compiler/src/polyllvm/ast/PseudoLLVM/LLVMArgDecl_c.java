package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMArgDecl_c extends LLVMNode_c implements LLVMArgDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected String name;

    public LLVMArgDecl_c(Position pos, LLVMTypeNode typeNode, String name,
            Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
        this.name = name;
    }

    public LLVMArgDecl_c(Position pos, LLVMTypeNode typeNode, String name) {
        this(pos, typeNode, name, null);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(typeNode, w, pp);
        w.write(" %");
        w.write(name);
    }

    @Override
    public String toString() {
        return typeNode + " %" + name;
    }

}
