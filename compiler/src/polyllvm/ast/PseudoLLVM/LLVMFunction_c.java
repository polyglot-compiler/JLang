package polyllvm.ast.PseudoLLVM;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

/**
 * @author Daniel
 *
 */
public class LLVMFunction_c extends LLVMNode_c implements LLVMFunction {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;
    protected List<LLVMBlock> blocks;
    protected List<LLVMArgDecl> args;
    protected LLVMTypeNode retType;

    public LLVMFunction_c(Position pos, String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks, Ext e) {
        super(pos, e);
        this.name = name;
        this.args = args;
        this.retType = retType;
        this.blocks = blocks;
    }

    public LLVMFunction_c(Position pos, String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks) {
        this(pos, name, args, retType, blocks, null);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("define ");
        print(retType, w, pp);
        w.write(" @" + name + "(");
        for (int i = 0; i < args.size() - 1; i++) {
            print(args.get(i), w, pp);
            w.write(", ");
        }
        if (args.size() != 0) {
            print(args.get(args.size() - 1), w, pp);
        }
        w.write(") {\n");
        for (LLVMBlock b : blocks) {
            print(b, w, pp);
        }
        w.write("}");
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("define ");
        s.append(retType.toString());
        s.append(" @");
        s.append(name);
        s.append("(");
        for (int i = 0; i < args.size() - 1; i++) {
            s.append(args.get(i));
            s.append(", ");
        }
        if (args.size() != 0) {
            s.append(args.get(args.size() - 1));
        }
        s.append(") {\n");
        for (LLVMBlock b : blocks) {
            s.append(b.toString());
        }
        s.append("\n}");
        return s.toString();
    }

    @Override
    public List<LLVMBlock> blocks() {
        return ListUtil.copy(blocks, false);
    }

    @Override
    public LLVMFunction blocks(List<LLVMBlock> bs) {
        return blocks(this, bs);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LLVMArgDecl> ads = visitList(args, v);
        LLVMTypeNode rt = visitChild(retType, v);
        List<LLVMBlock> bs = visitList(blocks, v);
        return reconstruct(this, bs, ads, rt);
    }

    /** Reconstruct the LLVM Function. */
    protected <N extends LLVMFunction_c> N reconstruct(N n, List<LLVMBlock> bs,
            List<LLVMArgDecl> ads, LLVMTypeNode tn) {
        n = blocks(n, bs);
        n = args(n, ads);
        n = retType(n, tn);
        return n;
    }

    protected <N extends LLVMFunction_c> N blocks(N n, List<LLVMBlock> bs) {
        bs = ListUtil.copy(bs, false);
        if (n.blocks == bs) return n;
        n = copyIfNeeded(n);
        n.blocks = bs;
        return n;
    }

    protected <N extends LLVMFunction_c> N args(N n, List<LLVMArgDecl> ad) {
        if (n.args == ad) return n;
        n = copyIfNeeded(n);
        n.args = ad;
        return n;
    }

    protected <N extends LLVMFunction_c> N retType(N n, LLVMTypeNode tn) {
        if (n.retType == tn) return n;
        n = copyIfNeeded(n);
        n.retType = tn;
        return n;
    }

}
