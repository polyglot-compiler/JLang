package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

import java.util.List;

public class LLVMStructureType_c extends LLVMTypeNode_c
        implements LLVMStructureType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMTypeNode> typeList;

    public LLVMStructureType_c(Position pos, List<LLVMTypeNode> typeList,
            Ext e) {
        super(pos, e);
        this.typeList = typeList;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("{");
        if (!typeList.isEmpty()) {
            for (int i = 0; i < typeList.size() - 1; i++) {
                print(typeList.get(i), w, pp);
                w.write(", ");
            }
            print(typeList.get(typeList.size() - 1), w, pp);
        }
        w.write("}");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LLVMTypeNode> tl = visitList(typeList, v);
        return reconstruct(this, tl);
    }

    protected <N extends LLVMStructureType_c> N reconstruct(N n,
            List<LLVMTypeNode> tl) {
        n = typeList(n, tl);
        return n;
    }

    protected <N extends LLVMStructureType_c> N typeList(N n,
            List<LLVMTypeNode> tl) {
        if (n.typeList == tl) return n;
        n = copyIfNeeded(n);
        n.typeList = tl;
        return n;
    }

}
