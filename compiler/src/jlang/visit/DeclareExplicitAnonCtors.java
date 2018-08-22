package jlang.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;

/**
 * Adds an explicit constructor to each anonymous class (including anonymous enum constants).
 * Explicit constructors are needed to hold class initialization code.
 */
public class DeclareExplicitAnonCtors extends DesugarVisitor {

    public DeclareExplicitAnonCtors(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
    }

    private ConstructorDecl buildAnonCtor(
            Position pos, ConstructorInstance oldCI, ParsedClassType anonClassType) {

        // Build the formals.
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

        return tnf.ConstructorDecl(pos, anonClassType, formals, body);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {

        if (n instanceof New) {
            New nw = (New) n;
            if (nw.body() != null) {
                ConstructorDecl ctor = buildAnonCtor(
                        nw.position(), nw.constructorInstance(), nw.anonType());
                nw = nw.body(nw.body().addMember(ctor));
                nw = nw.constructorInstance(ctor.constructorInstance());
            }
            n = nw;
        }

        if (n instanceof EnumConstantDecl) {
            EnumConstantDecl cd = (EnumConstantDecl) n;
            if (cd.body() != null) {
                ConstructorDecl ctor = buildAnonCtor(
                        cd.position(), cd.constructorInstance(), cd.type());
                cd = cd.body(cd.body().addMember(ctor));
                cd = cd.constructorInstance(ctor.constructorInstance());
            }
            n = cd;
        }

        return super.leaveDesugar(n);
    }
}
