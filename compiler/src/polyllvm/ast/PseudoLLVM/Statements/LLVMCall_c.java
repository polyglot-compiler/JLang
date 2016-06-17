package polyllvm.ast.PseudoLLVM.Statements;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMCall_c extends LLVMInstruction_c implements LLVMCall {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Pair<LLVMTypeNode, LLVMOperand>> arguments;
    protected LLVMVariable function;
    protected LLVMTypeNode retType;

    public LLVMCall_c(Position pos, LLVMVariable function,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments,
            LLVMTypeNode retType, Ext e) {
        super(pos, e);
        this.function = function;
        this.arguments = arguments;
        this.retType = retType;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (result != null) {
            print(result, w, pp);
            w.write(" = ");
        }
        w.write("call ");
        print(retType, w, pp);
        w.write(" ");
        print(function, w, pp);
        w.write("(");
        for (int i = 0; i < arguments.size() - 1; i++) {
            print(arguments.get(i).part1(), w, pp);
            w.write(" ");
            print(arguments.get(i).part2(), w, pp);
            w.write(", ");
        }
        if (arguments.size() != 0) {
            print(arguments.get(arguments.size() - 1).part1(), w, pp);
            w.write(" ");
            print(arguments.get(arguments.size() - 1).part2(), w, pp);
        }
        w.write(")");
    }

    @Override
    public LLVMCall result(LLVMVariable o) {
        return (LLVMCall) super.result(o);
    }

}
