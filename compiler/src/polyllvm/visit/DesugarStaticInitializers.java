package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Consolidates static class initialization code into a standalone function
 * that can be called prior to static field/method accesses.
 */
public class DesugarStaticInitializers extends DesugarVisitor {
    static final String STATIC_INIT_FUNC = "init$class";
    static final String STATIC_INIT_FLAG = "init$class$flag";

    public DesugarStaticInitializers(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        // TODO: Erase initializers here after they're moved, then remove LLVMInitializerExt.
        Position pos = ct.position();
        List<Stmt> initCode = new ArrayList<>();

        // Declare initialization flag for this class (as a static field).
        FieldDecl decl = tnf.FieldDecl(
                pos, ct, Flags.NONE.Static(), ts.Boolean(), STATIC_INIT_FLAG, /*init*/ null);
        cb = cb.addMember(decl);

        // Mark this class initialized.
        {
            Field initFlag = tnf.StaticField(pos, ct, STATIC_INIT_FLAG);
            Expr trueLit = nf.BooleanLit(pos, true).type(ts.Boolean());
            Stmt setTrue = tnf.EvalAssign(initFlag, trueLit);
            initCode.add(setTrue);
        }

        // Initialize superclass if necessary.
        if (!ct.flags().isInterface() && ct.superType() != null) {
            ReferenceType superCt = ct.superType().toReference();
            Field superInitFlag = tnf.StaticFieldForced(
                    superCt.position(), superCt,
                    Flags.NONE.Static(), ts.Boolean(), STATIC_INIT_FLAG);
            Call call = tnf.StaticCallForced(
                    pos, superCt, Flags.NONE.Static(), ts.Void(), STATIC_INIT_FUNC);
            Stmt test = tnf.If(tnf.Not(superInitFlag), nf.Eval(pos, call));
            initCode.add(test);
        }

        // Collect static initialization code.
        for (ClassMember member : cb.members()) {

            // Build initialization assignments for each initialized static field.
            // TODO: Could initialize constant values at compile time. Coordinate with LLVMFieldDeclExt.
            if (member instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) member;
                if (!fd.flags().isStatic() || fd.init() == null)
                    continue;
                Field field = tnf.StaticField(fd.position(), ct, fd.name());
                Stmt assign = tnf.EvalAssign(field, fd.init());
                initCode.add(assign);
            }

            // Build static initialization blocks.
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (!init.flags().isStatic())
                    continue;
                initCode.add(init.body());
            }
        }

        // Declare init method.
        MethodDecl initMethod = tnf.MethodDecl(pos,
                ct, Flags.NONE.Static(), ts.Void(), STATIC_INIT_FUNC,
                Collections.emptyList(),
                nf.Block(pos, initCode));
        cb = cb.addMember(initMethod);

        return cb;
    }
}
