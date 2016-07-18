package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMNullLiteral_c extends LLVMOperand_c
        implements LLVMNullLiteral {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;

    public LLVMNullLiteral_c(Position pos, LLVMTypeNode typeNode, Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("null");
    }

}
