package polyllvm.ast.PseudoLLVM.Statements;

import java.util.Optional;

import polyglot.util.CodeWriter;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMRet_c extends LLVMInstruction_c implements LLVMRet {
    private static final long serialVersionUID = SerialVersionUID.generate();
    protected Optional<Pair<LLVMTypeNode, LLVMOperand>> retInfo;

    public LLVMRet_c(Position pos) {
        super(pos, null);
        retInfo = Optional.empty();
    }

    public LLVMRet_c(Position pos, LLVMTypeNode t, LLVMOperand o) {
        super(pos, null);
        retInfo = Optional.of(new Pair<>(t, o));
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (retInfo.isPresent()) {
            w.write("ret ");
            print(retInfo.get().part1(), w, pp);
            w.write(" ");
            print(retInfo.get().part2(), w, pp);
        }
        else {
            w.write("ret void");
        }

    }

    @Override
    public String toString() {
        if (retInfo.isPresent()) {
            return "ret " + retInfo.get().part1() + " " + retInfo.get().part2();
        }
        else {
            return "ret void";
        }
    }

}
