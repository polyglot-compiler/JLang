package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public class LLVMAdd_c extends LLVMInstruction_c implements LLVMAdd {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMIntType intType;
    protected LLVMOperand left;
    protected LLVMOperand right;

    public LLVMAdd_c(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, e);
        result = r;
        intType = t;
        this.left = left;
        this.right = right;
    }

    public LLVMAdd_c(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right, Ext e) {
        this(pos, null, t, left, right, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result == null) {
            w.write("add ");
            print(intType, w, pp);
            w.write(" ");
            print(left, w, pp);
            w.write(", ");
            print(right, w, pp);
        }
        else {
            print(result, w, pp);
            w.write(" = add ");
            print(intType, w, pp);
            w.write(" ");
            print(left, w, pp);
            w.write(", ");
            print(right, w, pp);
        }
    }

    @Override
    public String toString() {
        if (result == null) {
            return "add " + intType + " " + left + ", " + right;
        }
        else {
            return result + " = add " + intType + " " + left + ", " + right;
        }
    }

    @Override
    public LLVMAdd result(LLVMVariable o) {
        return (LLVMAdd) super.result(o);
    }

    @Override
    public LLVMAdd intType(LLVMIntType i) {
        return intType(this, i);
    }

    @Override
    public LLVMAdd left(LLVMOperand l) {
        return left(this, l);
    }

    @Override
    public LLVMAdd right(LLVMOperand r) {
        return right(this, r);
    }

    protected <N extends LLVMAdd_c> N intType(N n, LLVMIntType intType) {
        if (n.intType == intType) return n;
        n = copyIfNeeded(n);
        n.intType = intType;
        return n;
    }

    protected <N extends LLVMAdd_c> N left(N n, LLVMOperand left) {
        if (n.left == left) return n;
        n = copyIfNeeded(n);
        n.left = left;
        return n;
    }

    protected <N extends LLVMAdd_c> N right(N n, LLVMOperand right) {
        if (n.right == right) return n;
        n = copyIfNeeded(n);
        n.right = right;
        return n;
    }

    @Override
    public LLVMVariable result() {
        return result;
    }

    @Override
    public LLVMIntType intType() {
        return intType;
    }

    @Override
    public LLVMOperand left() {
        return left;
    }

    @Override
    public LLVMOperand right() {
        return right;
    }

}
