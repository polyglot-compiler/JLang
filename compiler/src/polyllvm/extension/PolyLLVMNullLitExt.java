package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMNullLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        NullLit n = (NullLit) node();
        v.addTranslation(n, LLVMConstNull(LLVMUtils.llvmType(n.type())));
        return super.translatePseudoLLVM(v);
    }
}
