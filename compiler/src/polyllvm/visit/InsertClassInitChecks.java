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
                n = guardWithInitCheck(f, fi.container().toClass());
            }
        }

        // Guard class literals (effectively modeled as static fields).
        if (n instanceof ClassLit) {
            ClassLit cl = (ClassLit) n;
            Type type = cl.typeNode().type();
            if (type.isClass()) {
                n = guardWithInitCheck(cl, type.toClass());
            }
        }

        // Guard instance creation.
        if (n instanceof ConstructorDecl) {
            ConstructorDecl cd = (ConstructorDecl) n;
            // TODO: Might want to be able to guard native constructors too.
            if (cd.body() != null) {
                ConstructorInstance ci = cd.constructorInstance();
                Stmt check = buildInitCheck(cd.position(), ci.container().toClass());
                Block body = cd.body().prepend(check);
                n = cd.body(body);
            }
        }

        if (n instanceof MethodDecl) {
            MethodDecl md = (MethodDecl) n;
            MethodInstance mi = md.methodInstance();

            // Guard static methods.
            // TODO: Might want to be able to guard native static methods too.
            if (mi.flags().isStatic() && md.body() != null && !mi.name().equals(STATIC_INIT_FUNC)) {
                Stmt check = buildInitCheck(md.position(), mi.container().toClass());
                Block body = md.body().prepend(check);
                n = md.body(body);
            }

            // Initialize the string class at every entry point.
            // This lets us omit class init checks for string literals.
            boolean isEntryPoint = mi.name().equals("main")
                    && mi.flags().isPublic()
                    && mi.flags().isStatic()
                    && mi.formalTypes().size() == 1
                    && mi.formalTypes().get(0).equals(ts.arrayOf(ts.String()));
            if (isEntryPoint && md.body() != null) {
                Stmt check = buildInitCheck(md.position(), ts.String());
                Block body = md.body().prepend(check);
                n = md.body(body);
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
