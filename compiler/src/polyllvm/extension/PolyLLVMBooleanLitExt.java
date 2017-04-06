package polyllvm.extension;

import polyglot.ast.BooleanLit;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMBooleanLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        BooleanLit n = (BooleanLit) node();
        v.debugInfo.emitLocation(n);
        LLVMTypeRef type = v.utils.typeRef(n.type());
        LLVMValueRef val = LLVMConstInt(type, n.value() ? 1 : 0, /*sign-extend*/ 0);
        v.addTranslation(node(), val);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public void translateLLVMConditional(LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        BooleanLit n = (BooleanLit) node();
        v.debugInfo.emitLocation(n);
        if (n.value()) {
            LLVMBuildBr(v.builder, trueBlock);
        } else {
            LLVMBuildBr(v.builder, falseBlock);
        }
    }
}
