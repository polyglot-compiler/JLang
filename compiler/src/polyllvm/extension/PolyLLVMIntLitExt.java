package polyllvm.extension;

import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral_c;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMIntLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        IntLit n = (IntLit) node();
        v.addTranslation(n,
                         new LLVMIntLiteral_c(Position.compilerGenerated(),
                                              (int) n.value(),
                                              new PolyLLVMExt()));
        return super.translatePseudoLLVM(v);
    }
}
