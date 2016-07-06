package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMFloatLiteral_c extends LLVMOperand_c
        implements LLVMFloatLiteral {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected float value;

    public LLVMFloatLiteral_c(Position pos, LLVMTypeNode typeNode, float value,
            Ext e) {
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
        w.write(Float.toString(value));
    }

}
