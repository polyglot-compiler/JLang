package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMBitwiseBinaryInstruction_c extends LLVMBinaryOperandInstruction_c implements LLVMBitwiseBinaryInstruction{

    protected Op op;

    public LLVMBitwiseBinaryInstruction_c(Position pos, Op op, LLVMVariable result, LLVMIntType tn, LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, result, tn, left, right, e);
        this.op = op;
    }

    public LLVMBitwiseBinaryInstruction_c(Position pos, Op op, LLVMIntType tn, LLVMOperand left, LLVMOperand right, Ext e) {
        this(pos, op, null, tn, left, right, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        super.prettyPrint(w, pp);
        w.write(op.toString());
        w.write(" ");
        print(intType(), w, pp);
        w.write(" ");
        print(left, w, pp);
        w.write(", ");
        print(right, w, pp);
    }

    @Override
    public LLVMBitwiseBinaryInstruction result(LLVMVariable o) {
        return (LLVMBitwiseBinaryInstruction) super.result(o);
    }

    @Override
    public LLVMBitwiseBinaryInstruction intType(LLVMIntType i) {
        return typeNode(i);
    }

    @Override
    public LLVMBitwiseBinaryInstruction right(LLVMOperand r) {
        return (LLVMBitwiseBinaryInstruction) super.right(r);
    }

    @Override
    public LLVMBitwiseBinaryInstruction left(LLVMOperand l) {
        return (LLVMBitwiseBinaryInstruction) super.left(l);
    }

    @Override
    public LLVMBitwiseBinaryInstruction typeNode(LLVMTypeNode tn) {
        if (!(tn instanceof LLVMIntType)) {
            throw new InternalCompilerError("Trying to change integer "
                    + "addition to use non integer type.");
        }
        return (LLVMBitwiseBinaryInstruction) super.typeNode(tn);
    }


    @Override
    public LLVMTypeNode typeNode() {
        return intType();
    }

    @Override
    public LLVMIntType intType() {
        return (LLVMIntType) typeNode;
    }
}
