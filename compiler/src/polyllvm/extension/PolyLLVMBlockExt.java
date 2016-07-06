package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.ast.Stmt;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMBlockExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Block n = (Block) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<LLVMInstruction> x = new ArrayList<>(n.statements().size());
        for (Stmt s : n.statements()) {
            x.add((LLVMInstruction) v.getTranslation(s));
        }
        LLVMBlock translation = nf.LLVMBlock(Position.compilerGenerated(), x);
        v.addTranslation(node(), translation);
        return super.translatePseudoLLVM(v);
    }
}
