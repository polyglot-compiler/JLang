package polyllvm.extension;

import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMBranchExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Branch n = (Branch) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMBr br;
        if (n.kind() == Branch.BREAK && n.label() != null) {
            br = nf.LLVMBr(Position.compilerGenerated(),
                           nf.LLVMLabel(Position.compilerGenerated(),
                                        v.getLoopEnd(n.label())));
        }
        else if (n.kind() == Branch.BREAK && n.label() == null) {
            br = nf.LLVMBr(Position.compilerGenerated(),
                           nf.LLVMLabel(Position.compilerGenerated(),
                                        v.getLoopEnd()));
        }
        else if (n.kind() == Branch.CONTINUE && n.label() != null) {
            br = nf.LLVMBr(Position.compilerGenerated(),
                           nf.LLVMLabel(Position.compilerGenerated(),
                                        v.getLoopHead(n.label())));
        }
        else if (n.kind() == Branch.CONTINUE && n.label() == null) {
            br = nf.LLVMBr(Position.compilerGenerated(),
                           nf.LLVMLabel(Position.compilerGenerated(),
                                        v.getLoopHead()));
        }
        else {
            throw new InternalCompilerError("Unknown branch type: " + n.kind());
        }
        v.addTranslation(n, br);
        return super.translatePseudoLLVM(v);
    }
}
