package polyllvm.extension;

import polyglot.ast.Assign;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        Assign n = (Assign) node();
        assert n.left().type().typeEquals(n.right().type()) : "casts should already be added";
        assert n.operator().equals(Assign.ASSIGN) : "non-vanilla assignments should be de-sugared"; // TODO

        LLVMValueRef ptr = lang().translateAsLValue(n.left(), v);
        n.right().visit(v);
        LLVMValueRef expr = v.getTranslation(n.right());
        v.debugInfo.emitLocation(n);
        LLVMBuildStore(v.builder, expr, ptr);
        return n;
    }
}
