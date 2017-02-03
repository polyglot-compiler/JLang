package polyllvm.extension;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.ast.Stmt;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMBlockExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Block n = (Block) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> x = new ArrayList<>(n.statements().size());

        for (Stmt s : n.statements()) {
            LLVMNode translation = (LLVMNode) v.getTranslation(s);
            x.add((LLVMInstruction) translation);
        }

        LLVMBlock translation = nf.LLVMBlock(x);
        v.addTranslation(node(), translation);
        return super.translatePseudoLLVM(v);
    }
}
