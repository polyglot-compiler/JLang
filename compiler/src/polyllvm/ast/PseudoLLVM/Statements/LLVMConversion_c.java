package polyllvm.ast.PseudoLLVM.Statements;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
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
import polyllvm.visit.RemoveESeqVisitor;

public class LLVMConversion_c extends LLVMInstruction_c
        implements LLVMConversion {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Instruction instruction;

    protected LLVMTypeNode valueType;
    protected LLVMOperand value;
    protected LLVMTypeNode toType;

    public LLVMConversion_c(Position pos, Instruction instruction,
            LLVMVariable result, LLVMTypeNode valueType, LLVMOperand value,
            LLVMTypeNode toType, Ext e) {
        super(pos, e);
        this.instruction = instruction;
        this.result = result;
        this.valueType = valueType;
        this.value = value;
        this.toType = toType;
    }

    public LLVMConversion_c(Position pos, Instruction instruction,
            LLVMTypeNode valueType, LLVMOperand value, LLVMTypeNode toType,
            Ext e) {
        this(pos, instruction, null, valueType, value, toType, e);
    }

    @Override
    public LLVMTypeNode retType() {
        return toType;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        super.prettyPrint(w, pp);
        //<instruction> <ty> <value> to <ty2>
        w.write(instruction.toString());
        w.write(" ");
        print(valueType, w, pp);
        w.write(" ");
        print(value, w, pp);
        w.write(" to ");
        print(toType, w, pp);
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (value instanceof LLVMESeq) {
            LLVMESeq val = (LLVMESeq) value;

            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(val.instruction());
            instructions.add(reconstruct(this, valueType, val.expr(), toType));
            return nf.LLVMSeq(Position.compilerGenerated(), instructions);
        }
        return super.removeESeq(v);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode vt = visitChild(valueType, v);
        LLVMOperand val = visitChild(value, v);
        LLVMTypeNode tt = visitChild(toType, v);
        return reconstruct(this, vt, val, tt);
    }

    protected <N extends LLVMConversion_c> N reconstruct(N n, LLVMTypeNode vt,
            LLVMOperand val, LLVMTypeNode tt) {
        n = valueType(n, vt);
        n = value(n, val);
        n = toType(n, tt);
        return n;
    }

    protected <N extends LLVMConversion_c> N valueType(N n, LLVMTypeNode vt) {
        if (n.valueType == vt) return n;
        n = copyIfNeeded(n);
        n.valueType = vt;
        return n;
    }

    protected <N extends LLVMConversion_c> N value(N n, LLVMOperand val) {
        if (n.value == val) return n;
        n = copyIfNeeded(n);
        n.value = val;
        return n;
    }

    protected <N extends LLVMConversion_c> N toType(N n, LLVMTypeNode tt) {
        if (n.toType == tt) return n;
        n = copyIfNeeded(n);
        n.toType = tt;
        return n;
    }

}
