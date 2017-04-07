package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Builds class initializers at the top of each constructor.
 */
public class ClassInitializerVisitor extends ContextVisitor {
    private Deque<ClassDecl> classes = new ArrayDeque<>();

    public ClassInitializerVisitor(Job job, TypeSystem ts, NodeFactory nf) {
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
                        // Avoid duplicating initializer side-effects; the other
                        // constructor will handle initialization.
                        return super.leaveCall(n);
                    }
                }
            }

            // Build initialization assignments for each initialized non-static field.
            Block initCode = nf.Block(n.position());
            for (ClassMember member : classes.getLast().body().members()) {
                if (!(member instanceof FieldDecl))
                    continue;
                FieldDecl fd = (FieldDecl) member;
                if (fd.flags().isStatic() || fd.init() == null)
                    continue;
                Id id = nf.Id(fd.position(), fd.name());
                Special receiver = nf.Special(fd.position(), Special.THIS);
                Field field = nf.Field(fd.position(), receiver, id);
                Expr copy = (Expr) fd.init().copy();
                Assign assign = nf.FieldAssign(fd.position(), field, Assign.ASSIGN, copy);
                Eval eval = nf.Eval(fd.position(), assign);
                initCode = initCode.append(eval);
            }

            // Build initialization blocks.
            for (ClassMember member : classes.getLast().body().members()) {
                if (member instanceof Initializer) {
                    Initializer init = (Initializer) member;
                    if (!init.flags().isStatic()) {
                        initCode = initCode.append(init.body());
                    }
                }
            }

            return cd.body(cd.body().prepend(initCode));
        }

        return super.leaveCall(n);
    }
}
