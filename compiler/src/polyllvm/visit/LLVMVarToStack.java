package polyllvm.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;

public class LLVMVarToStack extends NodeVisitor {
    private PolyLLVMNodeFactory nf;
    private Map<String, LLVMTypeNode> allocations;
    private Map<String, LLVMTypeNode> arguments;

    public LLVMVarToStack(PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
        allocations = new HashMap<>();
        arguments = new HashMap<>();
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().llvmVarToStack(n, this);
    }

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    public void addArgument(String arg, LLVMTypeNode tn) {
        arguments.put(arg, tn);
    }

    public boolean isArg(String name) {
        return arguments.containsKey(name);
    }

    public void clearArguments() {
        arguments.clear();
    }

    public void addAllocation(String var, LLVMTypeNode tn) {
        System.out.println("Adding allocation: (" + var + ", " + tn + ")");
        allocations.put(var, tn);
    }

    public void clearAllocations() {
        allocations.clear();
    }

    public String varName(String var) {
        if (isArg(var)) {
            return "arg_" + var;
        }
        return var;
    }

    public List<LLVMInstruction> allocationInstructions() {
        List<LLVMInstruction> allocs = new ArrayList<>();
        for (Entry<String, LLVMTypeNode> e : allocations.entrySet()) {
            System.out.println("Adding allocation: " + e);
            LLVMAlloca a =
                    nf.LLVMAlloca(Position.compilerGenerated(), e.getValue());
            allocs.add(a.result(nf.LLVMVariable(Position.compilerGenerated(),
                                                varName(e.getKey()),
                                                e.getValue(),
                                                VarType.LOCAL)));
        }
        for (Entry<String, LLVMTypeNode> e : arguments.entrySet()) {
            LLVMAlloca a =
                    nf.LLVMAlloca(Position.compilerGenerated(), e.getValue());
            allocs.add(a.result(nf.LLVMVariable(Position.compilerGenerated(),
                                                varName(e.getKey()),
                                                e.getValue(),
                                                VarType.LOCAL)));
            allocs.add(nf.LLVMStore(Position.compilerGenerated(),
                                    e.getValue(),
                                    nf.LLVMVariable(Position.compilerGenerated(),
                                                    e.getKey(),
                                                    e.getValue(),
                                                    VarType.LOCAL),
                                    nf.LLVMVariable(Position.compilerGenerated(),
                                                    varName(e.getKey()),
                                                    e.getValue(),
                                                    VarType.LOCAL)));
        }
        return allocs;
    }

}
