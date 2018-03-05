package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.ProcedureCall;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.ProcedureInstance;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;

import java.util.ArrayList;
import java.util.List;

class PolyLLVMProcedureCallExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        ProcedureCall n = (ProcedureCall) node();

        // Is it a variable-argument procedure call?
        ProcedureInstance pi = n.procedureInstance();
        if (!JL5Flags.isVarArgs(pi.flags()))
            return super.desugar(v);

        // Is the variable argument already in proper form? Read the JLS! (15.12.4.2)
        int lastFormalIdx = pi.formalTypes().size() - 1;
        Type lastFormalT = pi.formalTypes().get(lastFormalIdx);
        assert lastFormalT.isArray();
        if (n.arguments().size() == pi.formalTypes().size()) {
            Type lastArgT = n.arguments().get(lastFormalIdx).type();
            if (v.ts.isImplicitCastValid(lastArgT, lastFormalT)) {
                return super.desugar(v);
            }
        }

        // Extract the variable arguments.
        List<Expr> args = n.arguments();
        List<Expr> varargs = new ArrayList<>(args.subList(lastFormalIdx, args.size()));
        args = new ArrayList<>(args.subList(0, lastFormalIdx));

        // Append the new vararg array.
        Expr varargArr = v.nf.ArrayInit(n.position(), varargs).type(lastFormalT);
        args.add(varargArr);
        return n.arguments(args);
    }
}
