package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.ParsedClassType;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.LinkedList;
import java.util.List;

/**
 * Builds class initializers at the top of each constructor.
 * Preserves typing.
 */
public class DesugarClassInitializers extends DesugarVisitor {

    public DesugarClassInitializers(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        List<ClassMember> members = cb.members();

        cb = mapConstructors(cb, (ctor) -> {
            List<Stmt> stmts = new LinkedList<>();
            LinkedList<Stmt> oldStmts = new LinkedList<>(ctor.body().statements());

            // Check for a call to another constructor.
            // The JLS ensures that a constructor call will be the first statement.
            if (oldStmts.peek() instanceof ConstructorCall) {
                ConstructorCall call = (ConstructorCall) oldStmts.pop();
                if (call.kind().equals(ConstructorCall.THIS)) {
                    // Avoid duplicating initializer side-effects; the other
                    // constructor will handle initialization.
                    return ctor;
                }
                // Keep the constructor call at the beginning.
                stmts.add(call);
            }

            // Create class initialization code for this constructor.
            for (ClassMember member : members) {

                // Build initialization assignments for each initialized non-static field.
                if (member instanceof FieldDecl) {
                    FieldDecl fd = (FieldDecl) member;
                    Position pos = fd.position();
                    if (fd.flags().isStatic() || fd.init() == null)
                        continue;
                    Special receiver = tnf.UnqualifiedThis(pos, ct);
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

            // Add back remaining constructor code.
            stmts.addAll(oldStmts);
            return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
        });

        return super.leaveClassBody(ct, cb);
    }
}
