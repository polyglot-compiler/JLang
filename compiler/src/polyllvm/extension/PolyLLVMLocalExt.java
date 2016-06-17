package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Local n = (Local) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMVariable translation =
                nf.LLVMVariable(Position.compilerGenerated(),
                                n.name(),
                                VarType.LOCAL,
                                null);
        v.addTranslation(node(), translation);
        return super.translatePseudoLLVM(v);
    }
}
