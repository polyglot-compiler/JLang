package polyllvm.extension;

import polyglot.ast.CharLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCharLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        CharLit n = (CharLit) node();
        LLVMValueRef val = LLVMConstInt(LLVMInt16Type(), n.value(), /* sign-extend */ 0);
        v.addTranslation(n, val);
        return super.translatePseudoLLVM(v);
    }
}
