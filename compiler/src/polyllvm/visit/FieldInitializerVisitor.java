package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Builds inline class field initializers at the top of each constructor.
 */
public class FieldInitializerVisitor extends ContextVisitor {
    private Deque<ClassDecl> classes = new ArrayDeque<>();

    public FieldInitializerVisitor(Job job, TypeSystem ts, NodeFactory nf) {
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
        if (n instanceof ClassDecl) {
            classes.removeLast();
        }
        else if (n instanceof ConstructorDecl) {
            ConstructorDecl cd = (ConstructorDecl) n;

            // Check for a call to another constructor.
            if (!cd.body().statements().isEmpty()) {
                // The JLS ensures that a constructor call will be the first statement
                // in a constructor body, if any.
                Stmt firstStmt = cd.body().statements().iterator().next();
                if (firstStmt instanceof ConstructorCall) {
                    ConstructorCall call = (ConstructorCall) firstStmt;
                    if (call.kind().equals(ConstructorCall.THIS)) {
                        // Avoid duplicating field initializer side-effects; the other
                        // constructor will handle initialization.
                        return super.leaveCall(n);
                    }
                }
            }

            // Build initialization assignments for each initialized non-static field.
            for (ClassMember member : classes.getLast().body().members()) {
                if (!(member instanceof FieldDecl))
                    continue;
                FieldDecl fd = (FieldDecl) member;
                if (fd.flags().isStatic() || fd.init() == null)
                    continue;
                Position pos = fd.position();
                Id id = nf.Id(pos, fd.name());
                Special receiver = nf.Special(pos, Special.THIS);
                Field field = nf.Field(pos, receiver, id);
                Expr copy = (Expr) fd.init().copy();
                Assign assign = nf.FieldAssign(pos, field, Assign.ASSIGN, copy);
                Eval eval = nf.Eval(pos, assign);
                cd = (ConstructorDecl) cd.body(cd.body().prepend(eval));
            }
            return cd;
        }

        return super.leaveCall(n);
    }
}
