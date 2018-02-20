package polyllvm.extension;

import polyglot.ast.ArrayInit;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMArrayInitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();

        // An ArrayInit's type is an array type whose base is the
        // "least common ancestor" of the elements; it can also be
        // the Null type when there is no element.
        assert n.type().isArray() || n.type().isNull();
        Type elemType = n.type().isArray()
                ? n.type().toArray().base()
                : v.typeSystem().Null();

        LLVMValueRef len = LLVMConstInt(
                v.utils.toLL(v.typeSystem().Int()), n.elements().size(), /*signExtend*/ 0);
        LLVMValueRef array = PolyLLVMNewArrayExt.translateNewArray(v, len, elemType);

        if (!n.elements().isEmpty()) {
            LLVMValueRef base = v.utils.buildJavaArrayBase(array, elemType);
            int idx = 0;
            for (Expr expr : n.elements()) {
                LLVMValueRef gep = v.utils.buildStructGEP(base, idx);
                LLVMBuildStore(v.builder, v.getTranslation(expr), gep);
                ++idx;
            }
        }

        v.addTranslation(n, array);
        return super.leaveTranslateLLVM(v);
    }
}
