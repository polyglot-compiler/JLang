package polyllvm.extension;

import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMBranchExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Branch n = (Branch) node();

        v.debugInfo.emitLocation(n);

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
