package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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
            classes.addLast((ClassDecl) n);
        return super.enterDesugar(n);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {
        if (n instanceof ClassDecl)
            classes.removeLast();
        if (!(n instanceof ConstructorDecl))
            return super.leaveDesugar(n);

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
                    return super.leaveDesugar(n);
                } else  {
                    // Initialization code should go after the call to super.
                    assert call.kind().equals(ConstructorCall.SUPER);
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
            Special receiver = tnf.This(pos, classes.getLast().type());
            Field field = tnf.Field(pos, receiver, fd.name());
            Stmt assign = tnf.EvalAssign(pos, field, fd.init());
            initCode = initCode.append(assign);
        }

        // Build initialization blocks.
        for (ClassMember member : classes.getLast().body().members()) {
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (init.flags().isStatic())
                    continue;
                initCode = initCode.append(init.body());
            }
        }

        Block newBody = cd.body().prepend(initCode);
        return cd.body(newBody);
    }
}
