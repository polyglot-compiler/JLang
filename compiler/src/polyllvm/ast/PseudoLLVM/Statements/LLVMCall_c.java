package polyllvm.ast.PseudoLLVM.Statements;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.ListUtil;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.RemoveESeqVisitor;

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

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMCall_c n = (LLVMCall_c) super.visitChildren(v);
        List<Pair<LLVMTypeNode, LLVMOperand>> args = new ArrayList<>();
        for (Pair<LLVMTypeNode, LLVMOperand> p : arguments) {
            args.add(new Pair<>(visitChild(p.part1(), v),
                                visitChild(p.part2(), v)));
        }
        LLVMVariable func = visitChild(function, v);
        LLVMTypeNode tn = visitChild(retType, v);
        return reconstruct(n, args, func, tn);
    }

    protected <N extends LLVMCall_c> N reconstruct(N n,
            List<Pair<LLVMTypeNode, LLVMOperand>> args, LLVMVariable func,
            LLVMTypeNode tn) {
        n = arguments(n, args);
        n = function(n, func);
        n = retType(n, tn);
        return n;
    }

    protected <N extends LLVMCall_c> N arguments(N n,
            List<Pair<LLVMTypeNode, LLVMOperand>> args) {
        if (n.arguments == args) return n;
        n = copyIfNeeded(n);
        n.arguments = args;
        return n;
    }

    protected <N extends LLVMCall_c> N function(N n, LLVMVariable f) {
        if (n.function == f) return n;
        n = copyIfNeeded(n);
        n.function = f;
        return n;
    }

    protected <N extends LLVMCall_c> N retType(N n, LLVMTypeNode tn) {
        if (n.retType == tn) return n;
        n = copyIfNeeded(n);
        n.retType = tn;
        return n;
    }

    @Override
    public List<Pair<LLVMTypeNode, LLVMOperand>> arguments() {
        return ListUtil.copy(arguments, false);
    }

    @Override
    public LLVMCall arguments(List<Pair<LLVMTypeNode, LLVMOperand>> args) {
        return arguments(this, args);
    }

    @Override
    public LLVMTypeNode retType() {
        return retType;
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        PolyLLVMNodeFactory nf = v.nodeFactory();

        List<LLVMInstruction> instructions = new ArrayList<>();
        List<LLVMInstruction> loads = new ArrayList<>();
        List<Pair<LLVMTypeNode, LLVMOperand>> newArgs = new ArrayList<>();
        for (Pair<LLVMTypeNode, LLVMOperand> p : arguments) {
            if (p.part2() instanceof LLVMESeq) {
                LLVMESeq e = (LLVMESeq) p.part2();
                LLVMTypeNode ptrType =
                        nf.LLVMPointerType(Position.compilerGenerated(),
                                           p.part1());
                LLVMVariable tempPointer =
                        PolyLLVMFreshGen.freshLocalVar(nf, ptrType);
                LLVMAlloca allocTemp =
                        nf.LLVMAlloca(Position.compilerGenerated(), p.part1());
                allocTemp = allocTemp.result(tempPointer);
                LLVMStore moveArg =
                        nf.LLVMStore(Position.compilerGenerated(),
                                     p.part1(),
                                     e.expr(),
                                     tempPointer);

                instructions.add(e.instruction());
                instructions.add(allocTemp);
                instructions.add(moveArg);

                LLVMVariable freshTemp =
                        PolyLLVMFreshGen.freshLocalVar(nf, p.part1());
                LLVMOperand ft = freshTemp;
                newArgs.add(new Pair<>(p.part1(), ft));

                LLVMLoad loadArg =
                        nf.LLVMLoad(Position.compilerGenerated(),
                                    freshTemp,
                                    p.part1(),
                                    tempPointer);
                loads.add(loadArg);
            }
            else {
                newArgs.add(p);
            }
        }
        instructions.addAll(loads);
        instructions.add(arguments(newArgs));
        return nf.LLVMSeq(Position.compilerGenerated(), instructions);
    }

}
