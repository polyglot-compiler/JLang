package polyllvm.ast.PseudoLLVM;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;

public class LLVMBlock_c extends LLVMInstruction_c implements LLVMBlock {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMInstruction> instructions;

    public LLVMBlock_c(Position pos, List<LLVMInstruction> instructions,
            Ext e) {
        super(pos, e);
        this.instructions = instructions;
    }

    @Override
    public LLVMBlock instructions(List<LLVMInstruction> instructions) {
        return instructions(this, new ArrayList<>(instructions));
    }

    @Override
    public LLVMBlock appendInstruction(LLVMInstruction i) {
        List<LLVMInstruction> l = new ArrayList<>(instructions.size() + 1);
        l.addAll(instructions);
        l.add(i);
        return instructions(this, l);
    }

    protected <N extends LLVMBlock_c> N instructions(N n,
            List<LLVMInstruction> is) {
        if (n.instructions == is) return n;
        n = copyIfNeeded(n);
        n.instructions = is;
        return n;
    }

    @Override
    public LLVMSeq instructions(PolyLLVMNodeFactory nf) {
        return nf.LLVMSeq(instructions);
    }

    @Override
    public List<LLVMInstruction> instructions() {
        return ListUtil.copy(instructions, false);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        for (int i = 0; i < instructions.size() - 1; i++) {
            print(instructions.get(i), w, pp);
            w.newline();
        }
        if (instructions.size() != 0) {
            print(instructions.get(instructions.size() - 1), w, pp);
        }
        w.newline();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (LLVMInstruction i : instructions) {
//            if (i == null)
//                throw new InternalCompilerError("Null instruction in LLVM block");
//            s.append(i.toString());
            if (i != null) {
                s.append(i.toString());
                s.append("\n");
            }
        }
        if (s.length() > 0) s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LLVMInstruction> is = visitList(instructions, v);
        return reconstruct(this, is);
    }

    /** Reconstruct the LLVM Block. */
    protected <N extends LLVMBlock_c> N reconstruct(N n,
            List<LLVMInstruction> is) {
        n = instructions(n, is);
        return n;
    }

    @Override
    public LLVMTypeNode retType() {
        throw new InternalCompilerError("");
    }
}
