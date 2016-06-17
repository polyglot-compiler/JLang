package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMInstruction expr = (LLVMInstruction) v.getTranslation(n.init());
        LLVMInstruction translation =
                expr.result(nf.LLVMVariable(Position.compilerGenerated(),
                                            n.name(),
                                            VarType.LOCAL,
                                            null));
        v.addTranslation(node(), translation);
        return super.translatePseudoLLVM(v);
    }
}

//Variable target = (Variable) n.left();
//PolyLLVMNodeFactory nf = v.nodeFactory();
//LLVMAdd expr = (LLVMAdd) v.getTranslation(n.right());
//
//if (target instanceof Local) {
//    Local local_var = (Local) target;
//    LLVMAdd translation =
//            expr.result(nf.LLVMVariable(Position.compilerGenerated(),
//                                        local_var.name(),
//                                        null));
//    v.addTranslation(node(), translation);
//}
//else {
//    throw new InternalCompilerError("Only assigns to local variables"
//            + " currently supported");
//}
//return super.translatePseudoLLVM(v);
