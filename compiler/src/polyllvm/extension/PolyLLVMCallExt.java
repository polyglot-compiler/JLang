package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Type;
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
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Call n = (Call) node();
        NodeFactory nf = v.nodeFactory();
        List<Expr> args = new ArrayList<>();
        List<? extends Type> types = n.methodInstance().formalTypes();
        for (int i = 0; i < n.arguments().size(); i++) {
            Expr expr = n.arguments().get(i);
            Type t = types.get(i);
            if (!t.equals(expr.type())) {
                CanonicalTypeNode castTypeNode =
                        nf.CanonicalTypeNode(Position.compilerGenerated(), t);
                Expr cast = nf.Cast(Position.compilerGenerated(),
                                    castTypeNode,
                                    expr)
                              .type(t);
                args.add(cast);
            }
            else {
                args.add(expr);
            }
        }
        return n.arguments(args);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Call n = (Call) node();

        PolyLLVMNodeFactory nf = v.nodeFactory();
        String mangledFuncName =
                PolyLLVMMangler.mangleMethodName(n.methodInstance()
                                                  .container()
                                                  .toString(),
                                                 n.name());
        LLVMTypeNode tn =
                PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
                                                           n.methodInstance()
                                                            .formalTypes(),
                                                           n.methodInstance()
                                                            .returnType());
        LLVMVariable func =
                nf.LLVMVariable(Position.compilerGenerated(),
                                mangledFuncName,
                                tn,
                                VarType.GLOBAL);
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
        LLVMCall llvmCall =
                nf.LLVMCall(Position.compilerGenerated(),
                            func,
                            arguments,
                            PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type()));
        LLVMTypeNode retTn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                                n.methodInstance()
                                                                 .returnType());
        LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, retTn);
        if (!n.type().isVoid()) {
            llvmCall = llvmCall.result(result);
        }

        v.addTranslation(n,
                         nf.LLVMESeq(Position.compilerGenerated(),
                                     llvmCall,
                                     result));
        return super.translatePseudoLLVM(v);
    }
}
