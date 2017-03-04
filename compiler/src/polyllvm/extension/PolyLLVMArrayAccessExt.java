package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();

        v.debugInfo.emitLocation(n);
        LLVMValueRef ptr = buildArrayElemPtr(n, v);
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "arr_load");
        v.addTranslation(n, load);
        return super.translatePseudoLLVM(v);
    }

    // Assumes n.array() and n.index() have already been translated.
    static LLVMValueRef buildArrayElemPtr(ArrayAccess n, LLVMTranslator v) {
        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef baseRaw = v.utils.buildStructGEP(v.builder, arr, 0, Constants.ARR_ELEM_OFFSET);
        LLVMTypeRef ptrType = v.utils.ptrTypeRef(v.utils.typeRef(n.type()));
        LLVMValueRef base = LLVMBuildCast(v.builder, LLVMBitCast, baseRaw, ptrType, "ptr_cast");
        LLVMValueRef offset = v.getTranslation(n.index());
        return v.utils.buildGEP(v.builder, base, offset);
    }
}
