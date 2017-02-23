package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        LLVMValueRef ptr = buildArrayElemPtr(n, v);
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "arr_load");
        v.addTranslation(n, load);
        return super.translatePseudoLLVM(v);
    }

    // Assumes n.array() and n.index() have already been translated.
    static LLVMValueRef buildArrayElemPtr(ArrayAccess n, PseudoLLVMTranslator v) {
        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef baseRaw = LLVMUtils.buildStructGEP(v.builder, arr, 0, Constants.ARR_ELEM_OFFSET);
        LLVMTypeRef ptrType = LLVMUtils.ptrTypeRef(LLVMUtils.typeRef(n.type(), v));
        LLVMValueRef base = LLVMBuildCast(v.builder, LLVMBitCast, baseRaw, ptrType, "ptr_cast");
        LLVMValueRef offset = v.getTranslation(n.index());
        return LLVMUtils.buildGEP(v.builder, base, offset);
    }
}
