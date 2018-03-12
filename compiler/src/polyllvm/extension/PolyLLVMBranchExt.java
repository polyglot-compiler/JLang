package polyllvm.extension;

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

        // If we are within an exception frame, we may need to detour through finally blocks first.
        v.buildFinallyBlockChain(loc.getTryCatchNestingLevel());

        LLVMBuildBr(v.builder, loc.getBlock());

        return super.leaveTranslateLLVM(v);
    }
}
