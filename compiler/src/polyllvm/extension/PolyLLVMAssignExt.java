package polyllvm.extension;

import polyglot.ast.Assign;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.Variable;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Assign n = (Assign) node();
        Variable target = (Variable) n.left();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMAdd expr = (LLVMAdd) v.getTranslation(n.right());

        if (target instanceof Local) {
            Local local_var = (Local) target;
            LLVMAdd translation =
                    expr.result(nf.LLVMVariable(Position.compilerGenerated(),
                                                local_var.name(),
                                                VarType.LOCAL,
                                                null));
            v.addTranslation(node(), translation);
        }
        else {
            throw new InternalCompilerError("Only assigns to local variables"
                    + " currently supported");
        }
        return super.translatePseudoLLVM(v);
    }
}
