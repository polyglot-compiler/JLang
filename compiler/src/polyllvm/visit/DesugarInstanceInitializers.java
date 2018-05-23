package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds class initializers at the top of each constructor.
 * Preserves typing.
 */
public class DesugarInstanceInitializers extends DesugarVisitor {
    private static final String INSTANCE_INIT_FUNC = "init$instance";

    public DesugarInstanceInitializers(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        // TODO: Erase initializers here after they're moved, then remove LLVMInitializerExt.

        if (ct.flags().isInterface())
            return super.leaveClassBody(ct, cb);
        assert !ct.constructors().isEmpty();

        // Collect class initialization code.
        List<Stmt> initCode = new ArrayList<>();
        for (ClassMember member : cb.members()) {

            // Build initialization assignments for each initialized non-static field.
            if (member instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) member;
                Position pos = fd.position();
                if (fd.flags().isStatic() || fd.init() == null)
                    continue;
                Special receiver = tnf.UnqualifiedThis(pos, ct);
                Field field = tnf.Field(pos, receiver, fd.name());
                Stmt assign = tnf.EvalAssign(field, fd.init());
                initCode.add(assign);
            }

            // Build initialization blocks.
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (init.flags().isStatic())
                    continue;
                initCode.add(init.body());
            }
        }

        if (initCode.isEmpty())
            return super.leaveClassBody(ct, cb); // Optimization.

        // Declare init method.
        MethodDecl initMethod = tnf.MethodDecl(
                ct.position(), ct,
                Flags.NONE.Private().Final(), ts.Void(), INSTANCE_INIT_FUNC,
                Collections.emptyList(),
                nf.Block(ct.position(), initCode));
        cb = cb.addMember(initMethod);

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

            // Call init function.
            Call callInitFunc = tnf.Call(
                    ct.position(),
                    tnf.UnqualifiedThis(ct.position(), ct),
                    INSTANCE_INIT_FUNC,
                    ct, ts.Void());
            stmts.add(nf.Eval(ct.position(), callInitFunc));

            // Add back remaining constructor code.
            stmts.addAll(oldStmts);
            return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
        });

        return super.leaveClassBody(ct, cb);
    }
}
