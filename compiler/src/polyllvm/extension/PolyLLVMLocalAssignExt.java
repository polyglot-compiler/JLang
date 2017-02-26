package polyllvm.extension;

import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMLocalAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        // Override in order to avoid emitting a load for the target.
        LocalAssign n = (LocalAssign) node();
        assert n.left().type().typeEquals(n.right().type()) : "casts should already be added";
        v.visitEdge(n, n.right());

        v.debugInfo.emitLocation(n);
        LLVMValueRef expr = v.getTranslation(n.right());
        LLVMValueRef ptr = v.getLocalVariable(n.left().name());
        LLVMBuildStore(v.builder, expr, ptr);
        return n;
    }
}
