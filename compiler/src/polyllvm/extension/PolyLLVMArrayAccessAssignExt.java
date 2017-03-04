package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMArrayAccessAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        // Override in order to avoid emitting a load for the array element.
        ArrayAccessAssign n = (ArrayAccessAssign) node();
        ArrayAccess arrAccess = n.left();

        v.visitEdge(arrAccess, arrAccess.array());
        v.visitEdge(arrAccess, arrAccess.index());

        v.debugInfo.emitLocation(n);

        LLVMValueRef ptr = PolyLLVMArrayAccessExt.buildArrayElemPtr(arrAccess, v);

        v.visitEdge(n, n.right());
        LLVMValueRef val = v.getTranslation(n.right());


        v.debugInfo.emitLocation(n);
        LLVMValueRef store = LLVMBuildStore(v.builder, val, ptr);
        v.addTranslation(n, store);
        return super.overrideTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ArrayAccessAssign n = (ArrayAccessAssign) node();
        ArrayAccess arrAccess = n.left();

        v.debugInfo.emitLocation(n);

        LLVMValueRef ptr = PolyLLVMArrayAccessExt.buildArrayElemPtr(arrAccess, v);
        LLVMValueRef val = v.getTranslation(n.right());
        LLVMValueRef store = LLVMBuildStore(v.builder, val, ptr);
        v.addTranslation(n, store);
        return super.translatePseudoLLVM(v);
    }

}
