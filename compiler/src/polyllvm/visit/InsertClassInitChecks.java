package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.Collections;

import static polyllvm.visit.DesugarStaticInitializers.STATIC_INIT_FLAG;
import static polyllvm.visit.DesugarStaticInitializers.STATIC_INIT_FUNC;

/** Inserts calls to static class initialization code prior to static field/method accesses. */
public class InsertClassInitChecks extends DesugarVisitor {

    public InsertClassInitChecks(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveDesugar(Node n) throws SemanticException {
        // From JLS 7, section 12.4.1.
        // A class or interface type T will be initialized immediately before the
        // first occurrence of any one of the following.
        // - T is a class and an instance of T is created.
        // - T is a class and a static method declared by T is invoked.
        // - A static field declared by T is assigned.
        // - A static field declared by T is used and the field is not a constant variable (§4.12.4).
        // - T is a top level class (§7.6), and an assert statement (§14.10)
        //     lexically nested within T (§8.1.3) is executed.

        // Note: we ignore the last case regarding assert statements.

        // Guard static fields.
        // TODO: We can leave constant final fields unguarded, according to the JLS. Coordinate with LLVMFieldDeclExt.
        if (n instanceof Field) {
            Field f = (Field) n;
            FieldInstance fi = f.fieldInstance();
            if (fi.flags().isStatic() && fi.container().isClass()) {
                return guardWithInitCheck(f, fi.container().toClass());
            }
        }

        // Guard `new` expressions.
        if (n instanceof New) {
            New nw = (New) n;
            ConstructorInstance ci = nw.constructorInstance();
            if (ci.container().isClass()) {
                return guardWithInitCheck(nw, ci.container().toClass());
            }
        }

        // Guard string literals, which are implicit object instantiations.
        if (n instanceof StringLit) {
            StringLit sl = (StringLit) n;
            return guardWithInitCheck(sl, ts.String());
        }

        // Guard static method calls.
        if (n instanceof Call) {
            Call c = (Call) n;
            MethodInstance mi = c.methodInstance();
            if (mi.flags().isStatic() && mi.container().isClass()) {
                return guardWithInitCheck(c, mi.container().toClass());
            }
        }

        // Guard class literals (effectively modeled as static fields).
        if (n instanceof ClassLit) {
            ClassLit cl = (ClassLit) n;
            Type type = cl.typeNode().type();
            if (type.isClass()) {
                return guardWithInitCheck(cl, type.toClass());
            }
        }

        // Initialize the containing class of every entry point.
        // Necessary to maintain our assumption that a class is initialized
        // before any code within its body runs.
        if (n instanceof MethodDecl) {
            MethodDecl md = (MethodDecl) n;
            MethodInstance mi = md.methodInstance();
            boolean isEntryPoint = mi.name().equals("main")
                    && mi.flags().isPublic()
                    && mi.flags().isStatic()
                    && mi.formalTypes().size() == 1
                    && mi.formalTypes().get(0).equals(ts.arrayOf(ts.String()));
            if (isEntryPoint) {
                Stmt check = buildInitCheck(md.position(), mi.container().toClass());
                Block body = md.body().prepend(check);
                return md.body(body);
            }
        }

        return super.leaveDesugar(n);
    }

    protected Expr guardWithInitCheck(Expr e, ClassType ct) {
        if (classes.peek() != null && extendsErased(classes.peek(), ct))
            return e; // Optimization. Recall that superclasses are initialized before subclasses.
        Stmt check = buildInitCheck(e.position(), ct);
        return tnf.ESeq(Collections.singletonList(check), e);
    }

    protected Stmt buildInitCheck(Position pos, ClassType ct) {
        ct = (ClassType) ct.declaration(); // Use declaration to avoid generics issues.
        Flags flags = Flags.NONE.Static();
        Field marker = tnf.StaticFieldForced(pos, ct, flags, ts.Boolean(), STATIC_INIT_FLAG);
        Expr cond = tnf.Not(marker);
        Call call = tnf.StaticCallForced(pos, ct, flags, ts.Void(), STATIC_INIT_FUNC);
        return tnf.If(cond, nf.Eval(pos, call));
    }

    /** Returns true if {@code ct} extends {@code sup}, ignoring generics. */
    protected boolean extendsErased(ClassType ct, ClassType sup) {
        ct = (ClassType) ct.declaration();
        sup = (ClassType) sup.declaration();
        //noinspection SimplifiableIfStatement
        if (ct.equals(sup))
            return true;
        return ct.superType() != null && extendsErased(ct.superType().toClass(), sup);
    }
}
