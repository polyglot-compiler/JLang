package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMSub_c extends LLVMBinaryOperandInstruction_c
        implements LLVMSub {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMSub_c(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, r, t, left, right, e);
    }

    public LLVMSub_c(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right, Ext e) {
        this(pos, null, t, left, right, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result == null) {
            w.write("sub ");
            print(intType(), w, pp);
            w.write(" ");
            print(left, w, pp);
            w.write(", ");
            print(right, w, pp);
        }
        else {
            print(result, w, pp);
            w.write(" = sub ");
            print(intType(), w, pp);
            w.write(" ");
            print(left, w, pp);
            w.write(", ");
            print(right, w, pp);
        }
    }

    @Override
    public String toString() {
        if (result == null) {
            return "sub " + intType() + " " + left + ", " + right;
        }
        else {
            return result + " = sub " + intType() + " " + left + ", " + right;
        }
    }

    @Override
    public LLVMSub result(LLVMVariable o) {
        return (LLVMSub) super.result(o);
    }

    @Override
    public LLVMSub intType(LLVMIntType i) {
        return typeNode(i);
    }

    @Override
    public LLVMSub right(LLVMOperand r) {
        return (LLVMSub) super.right(r);
    }

    @Override
    public LLVMSub left(LLVMOperand l) {
        return (LLVMSub) super.left(l);
    }

    @Override
    public LLVMSub typeNode(LLVMTypeNode tn) {
        if (!(tn instanceof LLVMIntType)) {
            throw new InternalCompilerError("Trying to change integer "
                    + "addition to use non integer type.");
        }
        return (LLVMSub) super.typeNode(tn);
    }

    @Override
    public LLVMIntType intType() {
        return (LLVMIntType) typeNode;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return intType();
    }
}
