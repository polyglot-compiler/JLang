package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMVariable_c extends LLVMOperand_c implements LLVMVariable {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;
    protected LLVMTypeNode typeNode;
    private VarKind varKind;

    public LLVMVariable_c(Position pos, String name, LLVMTypeNode tn, VarKind t,
            Ext e) {
        super(pos, e);
        this.name = name;
        varKind = t;
        typeNode = tn;
    }

    @Override
    public LLVMVariable name(String s) {
        return name(this, s);
    }

    protected <N extends LLVMVariable_c> N name(N n, String s) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        switch (varKind) {
        case LOCAL:
            w.write("%" + name);
            return;
        case GLOBAL:
            w.write("@" + name);
            return;
        }
        throw new InternalCompilerError("Switch statement not exaustive: "
                + varKind);
    }

    @Override
    public String toString() {
        switch (varKind) {
        case LOCAL:
            return "%" + name;
        case GLOBAL:
            return "@" + name;
        }
        throw new InternalCompilerError("Switch statement not exaustive: "
                + varKind);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }
}
