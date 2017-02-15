package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMLocalAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalAssign n = (LocalAssign) node();
        Local target = n.left();

        LLVMValueRef expr = v.getTranslation(n.right());
        LLVMBuildStore(v.builder, expr, v.getVariable(target.name()));

        return super.translatePseudoLLVM(v);
    }

}
