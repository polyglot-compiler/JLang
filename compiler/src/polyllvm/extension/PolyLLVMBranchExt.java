package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;

public class PolyLLVMBranchExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Branch n = (Branch) node();

        LLVMTranslator.ControlTransferLoc loc = n.kind() == Branch.CONTINUE
                ? v.getContinueLoc(n.label())
                : v.getBreakLoc(n.label());

        LLVM.LLVMBasicBlockRef dest = v.buildFinallyBlockChain(
                loc.getBlock(), loc.getTryCatchNestingLevel());

        v.addTranslation(n, LLVMBuildBr(v.builder, dest));
        return super.leaveTranslateLLVM(v);
    }
}
