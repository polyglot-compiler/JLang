package polyllvm.extension;

import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBranchExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Branch n = (Branch) node();
        v.debugInfo.emitLocation(n);
        LLVMBasicBlockRef block = n.kind() == Branch.CONTINUE
                ? v.getContinueBlock(n.label())
                : v.getBreakBlock(n.label());
        LLVMValueRef br = LLVMBuildBr(v.builder, block);
        v.addTranslation(n, br);
        return super.translatePseudoLLVM(v);
    }
}
