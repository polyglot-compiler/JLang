package polyllvm.extension;

import polyglot.ast.CharLit;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCharLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        CharLit n = (CharLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMTypeNode type = nf.LLVMIntType(Position.compilerGenerated(), 16);
        LLVMIntLiteral translation =
                nf.LLVMIntLiteral(Position.compilerGenerated(),
                                  type,
                                  n.value());
        v.addTranslation(n, translation);
        return super.translatePseudoLLVM(v);
    }
}
