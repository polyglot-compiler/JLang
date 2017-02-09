package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMConstNull;

public class PolyLLVMNullLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        NullLit n = (NullLit) node();
        v.addTranslation(n, LLVMConstNull(LLVMUtils.typeRef(n.type(), v.mod)));
        return super.translatePseudoLLVM(v);
    }
}
