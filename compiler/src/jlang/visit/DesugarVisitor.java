//Copyright (C) 2017 Cornell University

package jlang.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;
import jlang.util.TypedNodeFactory;

/** A visitor convenient for desugar passes. Rethrows semantic exceptions, for example. */
public abstract class DesugarVisitor extends NodeVisitor {
    public final Job job;
    public final JLangTypeSystem ts;
    public final JLangNodeFactory nf;
    public final TypedNodeFactory tnf;

    /** Convenience stack of enclosing classes (including anonymous classes). */
    protected final Deque<ClassType> classes = new ArrayDeque<>();

    /** Convenience stack of enclosing constructors. */
    protected final Deque<ConstructorDecl> constructors = new ArrayDeque<>();

    DesugarVisitor(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        tnf = new TypedNodeFactory(ts, nf);
    }

    /**
     * Retrieves the class type from the parent node of a class body.
     * The parent could be a class declaration, anonymous class, or an anonymous enum constant.
     */
    private ParsedClassType getClassType(Node parent) {
        if (parent instanceof ClassDecl) {
            return ((ClassDecl) parent).type();
        }
        else if (parent instanceof New) {
            return (ParsedClassType) ((New) parent).type().toClass();
        }
        else if (parent instanceof EnumConstantDecl) {
            return ((EnumConstantDecl) parent).type();
        }
        else {
            throw new InternalCompilerError("Unhandled class body container: " + parent.getClass());
        }
    }

    @Override
    public final NodeVisitor enter(Node parent, Node n) {

        // Push and enter class type.
        if (n instanceof ClassBody) {
            ClassBody cb = (ClassBody) n;
            ParsedClassType ct = getClassType(parent);
            classes.push(ct);
            enterClassBody(ct, cb);
        }

        // Pop constructor.
        if (n instanceof ConstructorDecl) {
            constructors.push((ConstructorDecl) n);
        }

        try {
            enterDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }

        return super.enter(n);
    }

    @Override
    public final NodeVisitor enter(Node n) {
        throw new InternalCompilerError("Should not be called");
    }

    @Override
    public final Node leave(Node parent, Node old, Node n, NodeVisitor v) {

        try {
            n = leaveDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }

        // Pop constructor.
        if (n instanceof ConstructorDecl) {
            constructors.pop();
        }

        // Pop and leave class type.
        if (n instanceof ClassBody) {
            ClassBody cb = (ClassBody) n;
            ParsedClassType ct = getClassType(parent);
            classes.pop();
            n = leaveClassBody(ct, cb);
        }

        return super.leave(old, n, v);
    }

    @Override
    public final Node leave(Node old, Node n, NodeVisitor v) {
        throw new InternalCompilerError("Should not be called");
    }

    /** Convenience method that unifies class declaration bodies and anonymous class bodies. */
    protected void enterClassBody(ParsedClassType ct, ClassBody body) {}

    protected void enterDesugar(Node parent, Node n) throws SemanticException {
        enterDesugar(n);
    }

    protected void enterDesugar(Node n) throws SemanticException {}

    /** Convenience method that unifies class declaration bodies and anonymous class bodies. */
    protected ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        return cb;
    }

    protected Node leaveDesugar(Node parent, Node n) throws SemanticException {
        return leaveDesugar(n);
    }

    protected Node leaveDesugar(Node n) throws SemanticException {
        return n;
    }

    /** Helper method for copying nodes. Recall that nodes must not alias each other in the AST. */
    @SuppressWarnings("unchecked")
    <T> T copy(Node n) {
        return (T) n.copy();
    }

    /** Helper method for concatenating lists */
    <U> List<U> concat(List<? extends U> a, List<? extends U> b) {
        List<U> res = new ArrayList<>();
        Stream.of(a, b).forEach(res::addAll);
        return res;
    }
    <U, T extends U> List<U> concat(T t, List<? extends U> l) {
        return concat(Collections.singletonList(t), l);
    }

    /** Helper method for updating class members. */
    ClassBody mapMembers(ClassBody cb, Function<ClassMember, ClassMember> f) {
        List<ClassMember> members = new ArrayList<>(cb.members());
        for (int i = 0; i < members.size(); ++i)
            members.set(i, f.apply(members.get(i)));
        return cb.members(members);
    }

    ClassBody mapConstructors(ClassBody cb, Function<ConstructorDecl, ConstructorDecl> f) {
        return mapMembers(cb, (m) -> {
            if (m instanceof ConstructorDecl)
                return f.apply((ConstructorDecl) m);
            else return m;
        });
    }

    /** Given a constructor declaration, transforms its `this` or `super` constructor call. */
    ConstructorDecl mapConstructorCall(
            ConstructorDecl cd, Function<ConstructorCall, ConstructorCall> f) {
        Block body = cd.body();
        List<Stmt> stmts = new ArrayList<>(body.statements());
        if (!stmts.isEmpty() && stmts.get(0) instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) stmts.get(0);
            cc = f.apply(cc);
            stmts.set(0, cc);
        }
        body = body.statements(stmts);
        return (ConstructorDecl) cd.body(body);
    }

    /**
     * Prepend formals to all constructors and constructor calls of a given class.
     * Of course, this does not update instantiations through new nor super constructor calls.
     */
    ClassBody prependConstructorFormals(
            ParsedClassType ct, ClassBody cb, List<Formal> extraFormals) {

        if (extraFormals.isEmpty())
            return cb;

        // Clear previous constructor instances.
        ct.setConstructors(Collections.emptyList());

        // Update constructor declarations and create new constructor instances.
        cb = mapConstructors(cb, (ctor) -> {
            // We construct new formals so they don't alias each other.
            List<Formal> extraFormalCopies = extraFormals.stream()
                    .map((f) -> tnf.Formal(f.position(), f.name(), f.declType(), f.flags()))
                    .collect(Collectors.toList());
            List<Formal> formals = concat(extraFormalCopies, ctor.formals());
            return tnf.ConstructorDecl(ctor.position(), ct, formals, ctor.body());
        });

        // Now that constructor instances are updated, we can update `this` constructor calls too.
        cb = mapConstructors(cb, (ctor) ->
            mapConstructorCall(ctor, (cc) -> {
                if (cc.kind().equals(ConstructorCall.THIS)) {
                    List<Local> extraArgs = ctor.formals().subList(0, extraFormals.size()).stream()
                            .map((extraFormal) -> tnf.Local(extraFormal.position(), extraFormal))
                            .collect(Collectors.toList());
                    List<Expr> args = concat(extraArgs, cc.arguments());
                    return tnf.ConstructorCall(cc.position(), cc.kind(), ct, args);
                } else {
                    return cc;
                }
            })
        );

        return cb;
    }

    /**
     * Adds the given fields to the class declaration, prepends corresponding formals to all
     * constructors, and initializes the fields with those formals.
     * Of course, this does not update instantiations through new nor super constructor calls.
     */
    ClassBody prependConstructorInitializedFields(
            ParsedClassType ct, ClassBody cb, List<FieldDecl> fields) {

        // Add field declarations.
        List<ClassMember> members = concat(fields, cb.members());
        cb = cb.members(members);

        // Prepend formals to constructors.
        List<Formal> formals = fields.stream()
                .map((f) -> tnf.Formal(f.position(), f.name(), f.declType(), Flags.FINAL))
                .collect(Collectors.toList());
        cb = prependConstructorFormals(ct, cb, formals);

        // Initialize fields.
        cb = mapConstructors(cb, (ctor) -> {
            List<Stmt> stmts = new ArrayList<>();
            LinkedList<Stmt> oldStmts = new LinkedList<>(ctor.body().statements());
            if (oldStmts.peek() instanceof ConstructorCall) {
                // Make sure the constructor call remains the first statement.
                ConstructorCall cc = (ConstructorCall) oldStmts.remove();
                if (cc.kind().equals(ConstructorCall.THIS))
                    return ctor; // The other constructor will initialize the fields.
                stmts.add(cc);
            }
            ctor.formals().stream().limit(fields.size()).forEach((f) -> {
                // Initialize this.f = f;
                Position pos = f.position();
                Local arg = tnf.Local(pos, f);
                Special thisClass = tnf.UnqualifiedThis(pos, ct);
                Field field = tnf.Field(pos, thisClass, f.name());
                stmts.add(tnf.EvalAssign(field, arg));
            });
            stmts.addAll(oldStmts);
            return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
        });

        return cb;
    }
}
