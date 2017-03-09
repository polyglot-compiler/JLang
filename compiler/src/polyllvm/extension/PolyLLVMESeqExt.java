package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Node;
import polyllvm.ast.ESeq;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;


public class PolyLLVMESeqExt extends PolyLLVMExt {

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ESeq n = (ESeq) node();
        v.addTranslation(n, v.getTranslation(n.expr()));
        return super.translatePseudoLLVM(v);
    }
}
