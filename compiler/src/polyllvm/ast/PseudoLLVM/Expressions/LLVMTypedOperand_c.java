package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMTypedOperand_c extends LLVMOperand_c
        implements LLVMTypedOperand {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected LLVMOperand operand;

    public LLVMTypedOperand_c(Position pos, LLVMOperand op, LLVMTypeNode tn,
            Ext e) {
        super(pos, e);
        typeNode = tn;
        operand = op;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(typeNode, w, pp);
        w.write(" ");
        print(operand, w, pp);
    }

    @Override
    public String toString() {
        return typeNode.toString() + " " + operand.toString();
    }
}
