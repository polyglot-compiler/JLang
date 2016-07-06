package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMFAdd_c extends LLVMBinaryOperandInstruction_c
        implements LLVMFAdd {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMFAdd_c(Position pos, LLVMVariable result, LLVMTypeNode tn,
            LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, result, tn, left, right, e);
    }

    public LLVMFAdd_c(Position pos, LLVMTypeNode tn, LLVMOperand left,
            LLVMOperand right, Ext e) {
        super(pos, tn, left, right, e);
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result != null) {
            print(result, w, pp);
            w.write(" = ");
        }
        w.write("fadd ");
        print(typeNode(), w, pp);
        w.write(" ");
        print(left, w, pp);
        w.write(", ");
        print(right, w, pp);

    }

}
