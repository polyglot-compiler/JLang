package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import polyglot.ast.Expr;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMProcedureCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Helper function that returns the translation of the arguments, inserting
     * bitcasts where necessary. Requires the translation of individual
     * arguments to have already been recorded by {@code v}.
     */
    protected List<LLVMValueRef> argsToLLVals(LLVMTranslator v, List<Expr> args,
            List<? extends Type> formalTys) {
        assert args.size() == formalTys.size();

        List<LLVMValueRef> x_args = new ArrayList<>(formalTys.size());
        for (int i = 0; i < formalTys.size(); ++i) {
            Type lhs = formalTys.get(i);
            Expr arg = args.get(i);
            x_args.add(v.utils.bitcastToLHS(arg, lhs));
        }
        return x_args;
    }

}
