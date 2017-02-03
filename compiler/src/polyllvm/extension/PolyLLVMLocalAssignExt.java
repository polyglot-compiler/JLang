package polyllvm.extension;

import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable.VarKind;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalAssign n = (LocalAssign) node();
        Local target = n.left();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMNode expr = v.getTranslation(n.right());

        if (!(expr instanceof LLVMOperand)) {
            throw new InternalCompilerError("Expression `" + n.right() + "` ("
                    + n.right().getClass()
                    + ") was not translated to an LLVMOperand " + "("
                    + v.getTranslation(n.right()) + ")");
        }

        LLVMTypeNode tn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        LLVMVariable ptr =
                nf.LLVMVariable(v.varName(target.name()), tn, VarKind.LOCAL);

        LLVMOperand value = (LLVMOperand) expr;
        LLVMStore store = nf.LLVMStore(tn, value, ptr);
        v.addTranslation(node(), store);

        return super.translatePseudoLLVM(v);
    }

}
