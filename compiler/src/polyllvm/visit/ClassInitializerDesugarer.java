package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Builds class initializers at the top of each constructor.
 * Preserves typing.
 */
public class ClassInitializerDesugarer extends ContextVisitor {
    private Deque<ClassDecl> classes = new ArrayDeque<>();

    public ClassInitializerDesugarer(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof ClassDecl)
            classes.addLast((ClassDecl) n);
        return super.enterCall(n);
    }

    @Override
    protected Node leaveCall(Node n) throws SemanticException {
        if (n instanceof ClassDecl)
            classes.removeLast();
        if (!(n instanceof ConstructorDecl))
            return super.leaveCall(n);

        Context c = context();
        ConstructorDecl cd = (ConstructorDecl) n;

        // Check for a call to another constructor.
        Block initCode = nf.Block(n.position());
        if (!cd.body().statements().isEmpty()) {
            // The JLS ensures that a constructor call will be the first statement
            // in a constructor body, if any.
            Stmt firstStmt = cd.body().statements().iterator().next();
            if (firstStmt instanceof ConstructorCall) {
                ConstructorCall call = (ConstructorCall) firstStmt;
                if (call.kind().equals(ConstructorCall.THIS)) {
                    // Avoid duplicating initializer side-effects; the other
                    // constructor will handle initialization.
                    return super.leaveCall(n);
                } else if (call.kind().equals(ConstructorCall.SUPER)) {
                    // Initialization code should go after the call to super.
                    initCode = initCode.append(firstStmt);
                    List<Stmt> stmts = cd.body().statements();
                    Block sansSuper = cd.body().statements(stmts.subList(1, stmts.size()));
                    cd = (ConstructorDecl) cd.body(sansSuper);
                }
            }
        }

        // Build initialization assignments for each initialized non-static field.
        for (ClassMember member : classes.getLast().body().members()) {
            if (!(member instanceof FieldDecl))
                continue;
            FieldDecl fd = (FieldDecl) member;
            Position pos = fd.position();
            if (fd.flags().isStatic() || fd.init() == null)
                continue;
            Id id = nf.Id(pos, fd.name());
            Special receiver = (Special) nf.Special(pos, Special.THIS)
                    .type(c.currentClass());
            Field field = (Field) nf.Field(pos, receiver, id)
                    .fieldInstance(fd.fieldInstance())
                    .type(fd.declType());
            Expr copy = (Expr) fd.init().copy();
            Assign assign = (Assign) nf.FieldAssign(pos, field, Assign.ASSIGN, copy)
                    .type(field.type());
            Eval eval = nf.Eval(pos, assign);
            initCode = initCode.append(eval);
        }

        // Build initialization blocks.
        for (ClassMember member : classes.getLast().body().members()) {
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (init.flags().isStatic())
                    continue;
                initCode = initCode.append((Stmt) init.body().copy());
            }
        }

        Block newBody = cd.body().prepend(initCode);
        return cd.body(newBody);
    }
}
