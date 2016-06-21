package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class LLVMVariable_c extends LLVMOperand_c implements LLVMVariable {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static enum VarType {
        LOCAL, GLOBAL
    }

    protected String name;
    private VarType varType;

    public LLVMVariable_c(Position pos, String name, VarType t, Ext e) {
        super(pos, e);
        this.name = name;
        varType = t;
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
        switch (varType) {
        case LOCAL:
            w.write("%" + name);
            return;
        case GLOBAL:
            w.write("@" + name);
            return;
        }
        throw new InternalCompilerError("Switch statement not exaustive: "
                + varType);
    }

    @Override
    public String toString() {
        switch (varType) {
        case LOCAL:
            return "%" + name;
        case GLOBAL:
            return "@" + name;
        }
        throw new InternalCompilerError("Switch statement not exaustive: "
                + varType);
    }
}
