package polyllvm.extension;

import polyglot.ast.CharLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCharLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        CharLit n = (CharLit) node();
        v.debugInfo.emitLocation(n);
        LLVMValueRef val = LLVMConstInt(LLVMInt16Type(), n.value(), /* sign-extend */ 0);
        v.addTranslation(n, val);
        return super.translatePseudoLLVM(v);
    }
}
