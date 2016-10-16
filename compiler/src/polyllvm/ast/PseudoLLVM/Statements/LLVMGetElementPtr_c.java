package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.util.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.RemoveESeqVisitor;

import java.util.ArrayList;
import java.util.List;

public class LLVMGetElementPtr_c extends LLVMInstruction_c
        implements LLVMGetElementPtr {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LLVMTypeNode typeNode;
    protected LLVMPointerType ptrType;
    protected LLVMOperand variable;
    protected List<LLVMTypedOperand> dereferenceList;

    public LLVMGetElementPtr_c(Position pos, LLVMOperand ptrVar,
            List<LLVMTypedOperand> l, Ext e) {
        super(pos, e);
        ptrType = (LLVMPointerType) ptrVar.typeNode();
        typeNode = ptrType.dereferenceType();
        variable = ptrVar;
        dereferenceList = ListUtil.copy(l, true);
    }

    @Override
    public LLVMTypeNode retType() {
        throw new InternalCompilerError("Figure out how to get return type of GEP instruction...");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LLVMTypeNode tn = visitChild(typeNode, v);
        LLVMPointerType ptr = visitChild(ptrType, v);
        LLVMOperand var = visitChild(variable, v);
        List<LLVMTypedOperand> dl = visitList(dereferenceList, v);
        return reconstruct(this, tn, ptr, var, dl);
    }

    protected <N extends LLVMGetElementPtr_c> N reconstruct(N n,
            LLVMTypeNode tn, LLVMPointerType ptr, LLVMOperand var,
            List<LLVMTypedOperand> dl) {
        n = typeNode(n, tn);
        n = ptrType(n, ptr);
        n = variable(n, var);
        n = dereferenceList(n, dl);
        return n;
    }

    protected <N extends LLVMGetElementPtr_c> N typeNode(N n, LLVMTypeNode tn) {
        if (n.typeNode == tn) return n;
        n = copyIfNeeded(n);
        n.typeNode = tn;
        return n;
    }

    protected <N extends LLVMGetElementPtr_c> N ptrType(N n,
            LLVMPointerType ptr) {
        if (n.ptrType == ptr) return n;
        n = copyIfNeeded(n);
        n.ptrType = ptr;
        return n;
    }

    protected <N extends LLVMGetElementPtr_c> N variable(N n, LLVMOperand var) {
        if (n.variable == var) return n;
        n = copyIfNeeded(n);
        n.variable = var;
        return n;
    }

    protected <N extends LLVMGetElementPtr_c> N dereferenceList(N n,
            List<LLVMTypedOperand> dl) {
        if (n.dereferenceList == dl) return n;
        n = copyIfNeeded(n);
        n.dereferenceList = dl;
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        super.prettyPrint(w, pp);
        w.write("getelementptr ");
        print(typeNode, w, pp);
        w.write(", ");
        print(ptrType, w, pp);
        w.write(" ");
        print(variable, w, pp);
        for (LLVMTypedOperand t : dereferenceList) {
            w.write(", ");
            print(t, w, pp);
        }
    }

    @Override
    public LLVMNode removeESeq(RemoveESeqVisitor v) {
        List<LLVMInstruction> instructions = new ArrayList<>();
        LLVMOperand newVariable = variable;
        List<LLVMTypedOperand> newDereferenceList = new ArrayList<>();

        if (variable instanceof LLVMESeq) {
            LLVMESeq var = (LLVMESeq) variable;

//            List<LLVMInstruction> instructions = new ArrayList<>();
            instructions.add(var.instruction());
            newVariable = var.expr();

//            instructions.add(reconstruct(this,
//                                         typeNode,
//                                         ptrType,
//                                         var.expr(),
//                                         dereferenceList));
//            return v.nodeFactory().LLVMSeq(instructions);
        }
        for (LLVMTypedOperand typedOperand : dereferenceList) {
            if (typedOperand.operand() instanceof LLVMESeq) {
                LLVMESeq operand = (LLVMESeq) typedOperand.operand();
                newDereferenceList.add(v.nodeFactory()
                                        .LLVMTypedOperand(operand.expr(),
                                                          typedOperand.typeNode()));
                instructions.add(operand.instruction());
            }
            else {
                newDereferenceList.add(typedOperand);
            }
        }
        instructions.add(reconstruct(this,
                                     typeNode,
                                     ptrType,
                                     newVariable,
                                     newDereferenceList));
        return v.nodeFactory().LLVMSeq(instructions);
    }

    @Override
    public LLVMGetElementPtr result(LLVMVariable o) {
        return (LLVMGetElementPtr) super.result(o);
    }

}
