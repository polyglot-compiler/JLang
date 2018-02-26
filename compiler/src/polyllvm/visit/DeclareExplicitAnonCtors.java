package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Adds an explicit constructor to each anonymous class. */
public class DeclareExplicitAnonCtors extends DesugarVisitor {

    public DeclareExplicitAnonCtors(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {
        if (!(n instanceof New))
            return super.leaveDesugar(n);

        New nw = (New) n;
        if (nw.anonType() == null)
            return super.leaveDesugar(n);

        // Build the parameters for the explicit constructor declaration.
        Position pos = nw.position();
        ConstructorInstance oldCI = nw.constructorInstance();
        List<Type> formalTypes = new ArrayList<>(oldCI.formalTypes());
        List<Formal> formals = new ArrayList<>();
        for (int i = 0; i < formalTypes.size(); ++i) {
            String name = "anon$formal$" + (i + 1);
            Type t = formalTypes.get(i);
            formals.add(tnf.Formal(pos, name, t, Flags.FINAL));
        }

        // Build the body.
        Block body;
        if (oldCI.container().toClass().flags().isInterface()) {
            // Interfaces don't have a constructor to call.
            body = nf.Block(pos);
        } else {
            List<Expr> args = formals.stream()
                    .map((f) -> tnf.Local(f.position(), f))
                    .collect(Collectors.toList());
            ConstructorCall superCall = tnf.ConstructorCall(
                    pos, ConstructorCall.SUPER, oldCI.container().toClass(), args);
            body = nf.Block(pos, superCall);
        }
        ConstructorDecl ctor = tnf.ConstructorDecl(pos, nw.anonType(), formals, body);
        nw = nw.body(nw.body().addMember(ctor));

        // Update the constructor instance of this node so that the
        // correct constructor is called.
        nw = nw.constructorInstance(ctor.constructorInstance());
        return nw;
    }
}
