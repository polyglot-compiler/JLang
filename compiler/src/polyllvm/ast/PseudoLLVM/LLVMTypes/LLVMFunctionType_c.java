package polyllvm.ast.PseudoLLVM.LLVMTypes;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

import java.util.List;

public class LLVMFunctionType_c extends LLVMTypeNode_c
        implements LLVMFunctionType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMTypeNode> formalTypes;
    protected LLVMTypeNode returnType;

    public LLVMFunctionType_c(Position pos, List<LLVMTypeNode> formalTypes,
            LLVMTypeNode returnType, Ext e) {
        super(pos, e);
        this.formalTypes = formalTypes;
        this.returnType = returnType;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(returnType, w, pp);
        w.write(" (");
        for (int i = 0; i < formalTypes.size(); i++) {
            print(formalTypes.get(i), w, pp);
            if (i != formalTypes.size() - 1) {
                w.write(", ");
            }
        }
        w.write(")*"); //TODO: Should keep star at end?
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LLVMTypeNode> ft = visitList(formalTypes, v);
        LLVMTypeNode tn = visitChild(returnType, v);
        return reconstruct(this, ft, tn);
    }

    protected <N extends LLVMFunctionType_c> N reconstruct(N n,
            List<LLVMTypeNode> ft, LLVMTypeNode tn) {
        n = formalTypes(n, ft);
        n = returnType(n, tn);
        return n;
    }

    protected <N extends LLVMFunctionType_c> N formalTypes(N n,
            List<LLVMTypeNode> ft) {
        if (n.formalTypes == ft) return n;
        n = copyIfNeeded(n);
        n.formalTypes = ft;
        return n;
    }

    protected <N extends LLVMFunctionType_c> N returnType(N n,
            LLVMTypeNode tn) {
        if (n.returnType == tn) return n;
        n = copyIfNeeded(n);
        n.returnType = tn;
        return n;
    }

    @Override
    public LLVMFunctionType prependFormalTypeNode(LLVMTypeNode tn) {
        List<LLVMTypeNode> ft = ListUtil.copy(formalTypes, false);
        ft.add(0, tn);
        return formalTypes(this, ft);
    }

}
