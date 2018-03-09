package polyllvm.extension;

import polyglot.ast.ArrayInit;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
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

        // Normally an empty ArrayInit could have ts.Null() as its base type,
        // but we fixed that in an earlier pass using the expected type of
        // this node with respect to its parent.
        if (!n.type().isArray())
            throw new InternalCompilerError("ArrayInit node does not have an array type");
        Type elemType = n.type().toArray().base();

        LLVMValueRef len = LLVMConstInt(
                v.utils.toLL(v.ts.Int()), n.elements().size(), /*signExtend*/ 0);
        LLVMValueRef array = PolyLLVMNewArrayExt.translateNewArray(v, len, elemType);

        if (!n.elements().isEmpty()) {
            LLVMValueRef base = v.obj.buildArrayBaseElementPtr(array, n.type().toArray());
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
