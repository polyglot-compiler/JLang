package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMDoubleLiteral_c extends LLVMOperand_c
        implements LLVMDoubleLiteral {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected double value;

    public LLVMDoubleLiteral_c(Position pos, LLVMTypeNode typeNode,
            double value, Ext e) {
        super(pos, e);
        this.typeNode = typeNode;
        this.value = value;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(Double.toString(value));
    }

}
