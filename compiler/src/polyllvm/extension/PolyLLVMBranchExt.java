package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
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

        LLVMValueRef br;
        if (n.kind() == Branch.BREAK && n.label() != null) {
            br = LLVMBuildBr(v.builder, v.getLoopEnd(n.label()));
        }
        else if (n.kind() == Branch.BREAK && n.label() == null) {
            br = LLVMBuildBr(v.builder, v.getLoopEnd());
        }
        else if (n.kind() == Branch.CONTINUE && n.label() != null) {
            br = LLVMBuildBr(v.builder, v.getLoopHead(n.label()));
        }
        else if (n.kind() == Branch.CONTINUE && n.label() == null) {
            br = LLVMBuildBr(v.builder, v.getLoopHead());
        }
        else {
            throw new InternalCompilerError("Unknown branch type: " + n.kind());
        }
        v.addTranslation(n, br);
        return super.translatePseudoLLVM(v);
    }
}
