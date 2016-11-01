package polyllvm.ast.PseudoLLVM;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMGlobalVarDeclaration_c extends LLVMGlobalDeclaration_c
        implements LLVMGlobalVarDeclaration {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;
    protected boolean isExtern;
    protected GlobalVariableKind kind;
    protected LLVMTypeNode typeNode;
    protected LLVMOperand initializerConstant;

    public LLVMGlobalVarDeclaration_c(Position pos, String name,
            boolean isExtern, GlobalVariableKind kind, LLVMTypeNode typeNode,
            LLVMOperand initializerConstant, Ext e) {
        super(pos, e);
        this.name = name;
        this.isExtern = isExtern;
        this.kind = kind;
        this.typeNode = typeNode;
        this.initializerConstant = initializerConstant;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("@");
        w.write(name);
        w.write(" = ");
        if (isExtern) {
            w.write("external ");
        }
        w.write(kind.toString());
        w.write(" ");
        print(typeNode, w, pp);
        if (initializerConstant != null && !isExtern) {
            w.write(" ");
            print(initializerConstant, w, pp);
        }
        else if (initializerConstant == null && !isExtern) {
            w.write(" zeroinitializer");
        }
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(typeNode, v);
        LLVMOperand ic = visitChild(initializerConstant, v);
        return reconstruct(this, tn, ic);
    }

    protected <N extends LLVMGlobalVarDeclaration_c> N reconstruct(N n,
            LLVMTypeNode tn, LLVMOperand ic) {
        n = typeNode(n, tn);
        n = initializerConstant(n, ic);
        return n;
    }

    protected <N extends LLVMGlobalVarDeclaration_c> N typeNode(N n,
            LLVMTypeNode typeNode) {
        if (n.typeNode == typeNode) return n;
        n = copyIfNeeded(n);
        n.typeNode = typeNode;
        return n;
    }

    protected <N extends LLVMGlobalVarDeclaration_c> N initializerConstant(N n,
            LLVMOperand ic) {
        if (n.initializerConstant == ic) return n;
        n = copyIfNeeded(n);
        n.initializerConstant = ic;
        return n;
    }

}
