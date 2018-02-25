package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.*;

/**
 * Builds class initializers at the top of each constructor.
 * Preserves typing.
 */
public class DesugarClassInitializers extends DesugarVisitor {
    private Deque<ClassDecl> classes = new ArrayDeque<>();

    public DesugarClassInitializers(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public NodeVisitor enterDesugar(Node n) throws SemanticException {
        if (n instanceof ClassDecl)
            classes.push((ClassDecl) n);
        return super.enterDesugar(n);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {
        if (n instanceof ClassDecl)
            classes.pop();
        if (!(n instanceof ConstructorDecl))
            return super.leaveDesugar(n);

        ConstructorDecl cd = (ConstructorDecl) n;
        LinkedList<Stmt> oldStmts = new LinkedList<>(cd.body().statements());
        List<Stmt> stmts = new ArrayList<>();

        // Check for a call to another constructor.
        // The JLS ensures that a constructor call will be the first statement.
        if (oldStmts.peek() instanceof ConstructorCall) {
            ConstructorCall call = (ConstructorCall) oldStmts.pop();
            if (call.kind().equals(ConstructorCall.THIS)) {
                // Avoid duplicating initializer side-effects; the other
                // constructor will handle initialization.
                return super.leaveDesugar(n);
            }
            // Keep the constructor call at the beginning.
            stmts.add(call);
        }

        for (ClassMember member : classes.peek().body().members()) {

            // Build initialization assignments for each initialized non-static field.
            if (member instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) member;
                Position pos = fd.position();
                if (fd.flags().isStatic() || fd.init() == null)
                    continue;
                Special receiver = tnf.UnqualifiedThis(pos, classes.peek().type());
                Field field = tnf.Field(pos, receiver, fd.name());
                Stmt assign = tnf.EvalAssign(pos, field, fd.init());
                stmts.add(assign);
            }

            // Build initialization blocks.
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (init.flags().isStatic())
                    continue;
                stmts.add(init.body());
            }
        }

        stmts.addAll(oldStmts);
        return cd.body(cd.body().statements(stmts));
    }
}
