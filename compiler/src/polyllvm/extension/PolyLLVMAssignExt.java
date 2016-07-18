package polyllvm.extension;

import polyglot.ast.Assign;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Expr;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.Variable;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Assign n = (Assign) node();
        Variable target = (Variable) n.left();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Expr expr = n.right();

        if (!target.type().equals(expr.type()) && target.type().isPrimitive()
                && expr.type().isPrimitive()) {
            CanonicalTypeNode ctn =
                    nf.CanonicalTypeNode(Position.compilerGenerated(),
                                         target.type());
            Expr cast = nf.Cast(Position.compilerGenerated(), ctn, expr)
                          .type(target.type());
            return n.right(cast);
        }

        return super.addPrimitiveWideningCasts(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Assign n = (Assign) node();
        Variable target = (Variable) n.left();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMNode expr = v.getTranslation(n.right());

        if (target instanceof Local) {
            if (expr instanceof LLVMOperand) {
                LLVMTypeNode tn =
                        PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
                LLVMVariable ptr = nf.LLVMVariable(Position.compilerGenerated(),
                                                   v.varName(((Local) target).name()),
                                                   tn,
                                                   VarType.LOCAL);

                LLVMOperand value = (LLVMOperand) expr;
                LLVMStore store = nf.LLVMStore(tn,
                                               value,
                                               ptr);
                v.addTranslation(node(), store);

            }
            else {
                throw new InternalCompilerError("Expression `" + n.right()
                        + "` (" + n.right().getClass()
                        + ") was not translated to an LLVMOperand " + "("
                        + v.getTranslation(n.right()) + ")");
            }
        }
        else {
            throw new InternalCompilerError("Only assigns to local variables"
                    + " currently supported");
        }
        return super.translatePseudoLLVM(v);
    }
}
