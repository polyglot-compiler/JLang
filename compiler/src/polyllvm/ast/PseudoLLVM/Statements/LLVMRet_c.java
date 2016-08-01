package polyllvm.ast.PseudoLLVM.Statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.RemoveESeqVisitor;

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

    @Override
    public Node visitChildren(NodeVisitor v) {
        if (retInfo.isPresent()) {
            LLVMRet_c n = (LLVMRet_c) super.visitChildren(v);
            LLVMTypeNode p1 = visitChild(retInfo.get().part1(), v);
            LLVMOperand p2 = visitChild(retInfo.get().part2(), v);
            Optional<Pair<LLVMTypeNode, LLVMOperand>> ri =
                    Optional.of(new Pair<>(p1, p2));
            return reconstruct(n, ri);
        }
        else {
            return super.visitChildren(v);
        }
    }

    protected <N extends LLVMRet_c> N reconstruct(N n,
            Optional<Pair<LLVMTypeNode, LLVMOperand>> ri) {
        n = retInfo(n, ri);
        return n;
    }

    protected <N extends LLVMRet_c> N retInfo(N n,
            Optional<Pair<LLVMTypeNode, LLVMOperand>> ri) {
        if (n.retInfo == ri) return n;
        n = copyIfNeeded(n);
        n.retInfo = ri;
        return n;
    }

    @Override
    public LLVMTypeNode retType() {
        throw new InternalCompilerError("LLVM instruction ret does not have a type");
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        if (!retInfo.isPresent()) {
            return this;
        }
        Pair<LLVMTypeNode, LLVMOperand> ri = retInfo.get();
        if (ri.part2() instanceof LLVMESeq) {
            LLVMESeq e = (LLVMESeq) ri.part2();

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(e.instruction());
            instructions.add(reconstruct(this,
                                         Optional.of(new Pair<>(ri.part1(),
                                                                e.expr()))));
            return v.nodeFactory().LLVMSeq(instructions);

        }
        else {
            return this;
        }

    }

}
