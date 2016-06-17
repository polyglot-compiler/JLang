package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Call n = (Call) node();
        System.out.println(n.methodInstance());
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMVariable func = nf.LLVMVariable(Position.compilerGenerated(),
                                            n.name(),
                                            VarType.GLOBAL,
                                            null);
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments = new ArrayList<>();
        for (Expr arg : n.arguments()) {
            if (arg == null) throw new InternalCompilerError("The argument "
                    + arg + " to function " + n.methodInstance()
                    + "is not translated");
            LLVMTypeNode typeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, arg.type());
            LLVMOperand operand = (LLVMOperand) v.getTranslation(arg);
            arguments.add(new Pair<>(typeNode, operand));
        }
        v.addTranslation(n,
                         nf.LLVMCall(Position.compilerGenerated(),
                                     func,
                                     arguments,
                                     PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                                        n.type()),
                                     null));
        return super.translatePseudoLLVM(v);
    }
}
