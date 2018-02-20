package polyllvm.visit;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.ProcedureCall;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.frontend.Job;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayList;
import java.util.List;

/** Desugars vararg method calls to explicitly construct an array for the vararg parameter. */
public class DesugarVarargs extends DesugarVisitor {

    public DesugarVarargs(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {

        // Is this a procedure call?
        if (!(n instanceof ProcedureCall))
            return super.leaveDesugar(n);

        // Is it a variable-argument procedure call?
        ProcedureCall pc = (ProcedureCall) n;
        ProcedureInstance pi = pc.procedureInstance();
        if (!JL5Flags.isVarArgs(pi.flags()))
            return super.leaveDesugar(n);


        // Is the variable argument already in proper form? Read the JLS! (15.12.4.2)
        int lastFormalIdx = pi.formalTypes().size() - 1;
        Type lastFormalT = pi.formalTypes().get(lastFormalIdx);
        assert lastFormalT.isArray();
        if (pc.arguments().size() == pi.formalTypes().size()) {
            Type lastArgT = pc.arguments().get(lastFormalIdx).type();
            if (ts.isImplicitCastValid(lastArgT, lastFormalT)) {
                return super.leaveDesugar(n);
            }
        }

        // Extract the variable arguments.
        List<Expr> args = pc.arguments();
        List<Expr> varargs = new ArrayList<>(args.subList(lastFormalIdx, args.size()));
        args = new ArrayList<>(args.subList(0, lastFormalIdx));

        // Append the new vararg array.
        Expr varargArr = nf.ArrayInit(n.position(), varargs).type(lastFormalT);
        args.add(varargArr);
        return pc.arguments(args);
    }
}
