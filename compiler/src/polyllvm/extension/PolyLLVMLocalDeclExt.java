package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMTypeNode typeNode = (LLVMTypeNode) v.getTranslation(n.type());
        v.addAllocation(n.name(), typeNode);

        if (n.init() == null) {
            return super.translatePseudoLLVM(v);
        }

        LLVMNode decl = v.getTranslation(n.init());

        if (!(decl instanceof LLVMOperand)) {
            throw new InternalCompilerError("Initializer " + n.init() + " ("
                    + n.init().getClass() + ") was not translated to an"
                    + " LLVMOperand, it was translated: " + decl);
        }
        LLVMVariable ptr =
                nf.LLVMVariable(v.varName(n.name()), typeNode, VarType.LOCAL);
        LLVMStore store = nf.LLVMStore(typeNode, (LLVMOperand) decl, ptr);
        v.addTranslation(n, store);

        return super.translatePseudoLLVM(v);
    }
}
