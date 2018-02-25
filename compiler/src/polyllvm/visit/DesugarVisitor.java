package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A visitor convenient for desugar passes. Rethrows semantic exceptions, for example. */
public abstract class DesugarVisitor extends NodeVisitor {
    protected Job job;
    protected PolyLLVMTypeSystem ts;
    protected PolyLLVMNodeFactory nf;
    protected TypedNodeFactory tnf;

    DesugarVisitor(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public final Node override(Node parent, Node n) {
        try {
            return overrideDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    @Override
    public final Node override(Node n) {
        throw new InternalCompilerError("Should not be called");
    }

    @Override
    public final NodeVisitor enter(Node parent, Node n) {
        try {
            enterDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        return this;
    }

    @Override
    public final NodeVisitor enter(Node n) {
        throw new InternalCompilerError("Should not be called");
    }

    @Override
    public final Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        try {
            return leaveDesugar(parent, n);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    @Override
    public final Node leave(Node old, Node n, NodeVisitor v) {
        throw new InternalCompilerError("Should not be called");
    }

    public Node overrideDesugar(Node Parent, Node n) throws SemanticException {
        return overrideDesugar(n);
    }

    public Node overrideDesugar(Node n) throws SemanticException {
        return null;
    }

    public NodeVisitor enterDesugar(Node parent, Node n) throws SemanticException {
        return enterDesugar(n);
    }

    public NodeVisitor enterDesugar(Node n) throws SemanticException {
        return this;
    }

    public Node leaveDesugar(Node parent, Node n) throws SemanticException {
        return leaveDesugar(n);
    }

    public Node leaveDesugar(Node n) throws SemanticException {
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
    ClassDecl mapMembers(ClassDecl cd, Function<ClassMember, ClassMember> f) {
        List<ClassMember> members = new ArrayList<>(cd.body().members());
        for (int i = 0; i < members.size(); ++i)
            members.set(i, f.apply(members.get(i)));
        return cd.body(cd.body().members(members));
    }

    ClassDecl mapConstructors(ClassDecl cd, Function<ConstructorDecl, ConstructorDecl> f) {
        return mapMembers(cd, (m) -> {
            if (m instanceof ConstructorDecl)
                return f.apply((ConstructorDecl) m);
            else return m;
        });
    }

    /**
     * Prepend formals to all constructors and constructor calls of a given class.
     * Of course, this does not update instantiations through new nor super constructor calls.
     */
    ClassDecl prependConstructorFormals(ClassDecl cd, List<Formal> extraFormals) {
        if (extraFormals.isEmpty())
            return cd;

        // Clear previous constructor instances.
        ParsedClassType container = cd.type();
        container.setConstructors(Collections.emptyList());

        // Update constructor declarations and create new constructor instances.
        cd = mapConstructors(cd, (ctor) -> {
            // We construct new formals so they don't alias each other.
            List<Formal> extraFormalCopies = extraFormals.stream()
                    .map((f) -> tnf.Formal(f.position(), f.name(), f.declType(), f.flags()))
                    .collect(Collectors.toList());
            List<Formal> formals = concat(extraFormalCopies, ctor.formals());
            return tnf.ConstructorDecl(ctor.position(), container, formals, ctor.body());
        });

        // Now that constructor instances are updated, we can update constructor calls too.
        cd = mapConstructors(cd, (ctor) -> {
            Block body = ctor.body();
            List<Stmt> stmts = new ArrayList<>(body.statements());
            if (!stmts.isEmpty() && stmts.get(0) instanceof ConstructorCall) {
                ConstructorCall cc = (ConstructorCall) stmts.get(0);
                if (cc.kind().equals(ConstructorCall.THIS)) {
                    List<Local> extraArgs = ctor.formals().subList(0, extraFormals.size()).stream()
                            .map((extraFormal) -> tnf.Local(extraFormal.position(), extraFormal))
                            .collect(Collectors.toList());
                    List<Expr> args = concat(extraArgs, cc.arguments());
                    cc = tnf.ConstructorCall(cc.position(), cc.kind(), container, args);
                }
                stmts.set(0, cc);
            }
            body = body.statements(stmts);
            return (ConstructorDecl) ctor.body(body);
        });

        return cd;
    }

    /**
     * Adds the given fields to the class declaration, prepends corresponding formals to all
     * constructors, and initializes the fields with those formals.
     * Of course, this does not update instantiations through new nor super constructor calls.
     */
    ClassDecl prependConstructorInitializedFields(ClassDecl cd, List<FieldDecl> fields) {

        // Add field declarations.
        List<ClassMember> members = concat(fields, cd.body().members());
        cd = cd.body(cd.body().members(members));

        // Prepend formals to constructors.
        List<Formal> formals = fields.stream()
                .map((f) -> tnf.Formal(f.position(), f.name(), f.declType(), Flags.FINAL))
                .collect(Collectors.toList());
        cd = prependConstructorFormals(cd, formals);

        // Initialize fields.
        ClassType container = cd.type();
        cd = mapConstructors(cd, (ctor) -> {
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
                Special thisClass = tnf.UnqualifiedThis(pos, container);
                Field field = tnf.Field(pos, thisClass, f.name());
                stmts.add(tnf.EvalAssign(pos, field, arg));
            });
            stmts.addAll(oldStmts);
            return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
        });

        return cd;
    }
}
