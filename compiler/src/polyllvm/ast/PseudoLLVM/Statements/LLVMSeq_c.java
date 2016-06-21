package polyllvm.ast.PseudoLLVM.Statements;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMSeq_c extends LLVMInstruction_c implements LLVMSeq {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMInstruction> instrutctions;

    public LLVMSeq_c(Position pos, List<LLVMInstruction> instructions, Ext e) {
        super(pos, e);
        instrutctions = instructions;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        for (LLVMInstruction i : instrutctions) {
            print(i, w, pp);
            w.newline();
        }
    }

}
