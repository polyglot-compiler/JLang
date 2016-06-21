package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public class LLVMICmp_c extends LLVMCmp_c implements LLVMICmp {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public LLVMICmp_c(Position pos, LLVMVariable result, IConditionCode cc,
            LLVMIntType tn, LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, result, cc, tn, left, right, e);
    }

    public LLVMICmp_c(Position pos, IConditionCode cc, LLVMIntType tn,
            LLVMOperand left, LLVMOperand right, Ext e) {
        super(pos, cc, tn, left, right, e);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result != null) {
            print(result, w, pp);
            w.write(" = ");
        }
        w.write("icmp ");
        w.write(conditionCode.toString() + " ");
        print(typeNode(), w, pp);
        w.write(" ");
        print(left(), w, pp);
        w.write(", ");
        print(right(), w, pp);
    }

}
