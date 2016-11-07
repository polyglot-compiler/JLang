package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

import java.util.List;

public class LLVMSeq_c extends LLVMInstruction_c implements LLVMSeq {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMInstruction> instructions;

    public LLVMSeq_c(Position pos, List<LLVMInstruction> instructions, Ext e) {
        super(pos, e);
        this.instructions = instructions;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        for (int i = 0; i < instructions.size(); i++) {
            LLVMInstruction instr = instructions.get(i);
            print(instr, w, pp);
            if (i != instructions.size() - 1) {
                w.newline();
            }
        }
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMSeq_c n = (LLVMSeq_c) super.visitChildren(v);
        List<LLVMInstruction> is = visitList(instructions, v);
        return reconstruct(n, is);
    }

    protected <N extends LLVMSeq_c> N reconstruct(N n,
            List<LLVMInstruction> is) {
        n = instructions(n, is);
        return n;
    }

    protected <N extends LLVMSeq_c> N instructions(N n,
            List<LLVMInstruction> is) {
        if (n.instructions == is) return n;
        n = copyIfNeeded(n);
        n.instructions = is;
        return n;
    }

    public List<LLVMInstruction> instructions(){
        return ListUtil.copy(instructions, false);
    }

    @Override
    public LLVMTypeNode retType() {
        throw new InternalCompilerError("Pseudo LLVM instruction SEQ does not have a type");
    }
}
