package polyllvm.extension;

import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMIntLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        IntLit n = (IntLit) node();
        assert n.type().isLongOrLess();
        LLVMValueRef res = LLVMConstInt(LLVMUtils.typeRef(n.type(), v),
                                        n.value(), /* sign-extend */ 0);
        v.addTranslation(n, res);
        return super.translatePseudoLLVM(v);
    }
}
