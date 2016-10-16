package polyllvm.extension;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.ProcedureCall;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMProcedureCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Pair<LLVMTypeNode, LLVMOperand>> setupArguments(
            PseudoLLVMTranslator v, ProcedureCall n, PolyLLVMNodeFactory nf,
            LLVMOperand thisTranslation, LLVMTypeNode thisType) {
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                setupArguments(v, n, nf);

        //Add this as the first argument
        if (thisTranslation instanceof LLVMESeq) {
            arguments.add(0, new Pair<>(thisType,
                                        ((LLVMESeq) thisTranslation).expr()));
        }
        else {
            arguments.add(0, new Pair<>(thisType, thisTranslation));
        }

        return arguments;
    }

    protected List<Pair<LLVMTypeNode, LLVMOperand>> setupArguments(
            PseudoLLVMTranslator v, ProcedureCall n, PolyLLVMNodeFactory nf) {
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments = new ArrayList<>();

        for (Expr arg : n.arguments()) {
            if (arg == null) throw new InternalCompilerError("The argument "
                    + arg + " to function " + n.procedureInstance()
                    + "is not translated");
            LLVMTypeNode typeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, arg.type());
            LLVMOperand operand = (LLVMOperand) v.getTranslation(arg);
            arguments.add(new Pair<>(typeNode, operand));
        }

        return arguments;
    }

    protected Pair<LLVMCall, LLVMVariable> setupCall(PseudoLLVMTranslator v,
            ProcedureCall n, PolyLLVMNodeFactory nf, LLVMVariable functionPtr,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments, boolean isMethod) {

        LLVMTypeNode callTypeNode;
        if (n instanceof Call) {
            callTypeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, ((Call) n).type());
        }
        else {
            callTypeNode = nf.LLVMVoidType();
        }

        LLVMCall llvmCall = nf.LLVMCall(functionPtr, arguments, callTypeNode);

        LLVMVariable result = null;
        if (n instanceof Call && !((Call) n).type().isVoid()) {

            LLVMTypeNode retTn =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf,
                                                       ((Call) n).methodInstance()
                                                                 .returnType());
            result = PolyLLVMFreshGen.freshLocalVar(nf, retTn);
            llvmCall = llvmCall.result(result);
        }

        if (n instanceof Call && !(((Call) n).target() instanceof Expr)) {
            v.addStaticCall(llvmCall);
        }

        return new Pair<>(llvmCall, result);
    }

}
