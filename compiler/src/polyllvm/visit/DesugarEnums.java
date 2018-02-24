package polyllvm.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Desugar Java enum classes into normal classes. Derived from
 * {@link polyglot.ext.jl5.visit.RemoveEnums}, but heavily modified for PolyLLVM.
 * Preserves typing.
 */
public class DesugarEnums extends DesugarVisitor {

    public DesugarEnums(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveDesugar(Node parent, Node n) {

        // Enum declaration.
        if (n instanceof ClassDecl && JL5Flags.isEnum(((ClassDecl) n).flags())) {
            return translateEnumDecl((ClassDecl) n);
        }

        // Enum constant in a switch case.
        if (n instanceof EnumConstant && parent instanceof Case) {
            return translateEnumCase((EnumConstant) n);
        }

        // Enum expression in a switch.
        boolean isEnumExpr = n instanceof Expr
                && ((Expr) n).type().isClass()
                && JL5Flags.isEnum(((Expr) n).type().toClass().flags());
        if (isEnumExpr && parent instanceof Switch) {
            assert ((Switch) parent).expr() == n;
            return translateEnumSwitchExpr((Expr) n);
        }

        return n;
    }

    /** Convert an enum switch case to an integer constant case using the ordinal. */
    private Expr translateEnumCase(EnumConstant ec) {
        return nf.IntLit(ec.position(), IntLit.INT, ec.enumInstance().ordinal()).type(ts.Int());
    }

    /** Convert an enum switch expression to a call to {@link Enum#ordinal()}. */
    private Expr translateEnumSwitchExpr(Expr expr) {
        ClassType enumClass = expr.type().toClass().superType().toClass();
        return tnf.Call(expr.position(), expr, "ordinal", enumClass, ts.Int());
    }

    /** Convert enum declaration to class declaration. */
    private Node translateEnumDecl(ClassDecl enumDecl) {

        // Prepend name and ordinal to each constructor.
        List<Formal> formals = Arrays.asList(
                tnf.Formal(enumDecl.position(), "enum$name", ts.String(), Flags.FINAL),
                tnf.Formal(enumDecl.position(), "enum$ordinal", ts.Int(), Flags.FINAL));
        enumDecl = prependConstructorFormals(enumDecl, formals);

        // Include name and ordinal in super constructor calls.
        ClassType container = enumDecl.type();
        ClassType enumClass = container.toClass().superType().toClass();
        enumDecl = mapConstructors(enumDecl, (ctor) -> {
            LinkedList<Stmt> stmts = new LinkedList<>(ctor.body().statements());
            ConstructorCall oldCC = stmts.peek() instanceof ConstructorCall
                    ? (ConstructorCall) stmts.pop()
                    : null;

            if (oldCC == null || oldCC.kind().equals(ConstructorCall.SUPER)) {
                // Create super constructor call with name and ordinal as arguments.
                List<Expr> locals = ctor.formals().stream()
                        .limit(formals.size())
                        .map((f) -> tnf.Local(f.position(), f))
                        .collect(Collectors.toList());
                ConstructorCall newCC = tnf.ConstructorCall(
                        ctor.position(), ConstructorCall.SUPER, enumClass, locals);
                stmts.push(newCC);
                return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
            }
            else {
                // No super constructor call; constructor remains unchanged.
                return ctor;
            }
        });

        // Convert enum constant declarations into static fields, and
        // include name and ordinal in the corresponding initializers.
        enumDecl = mapMembers(enumDecl, (m) -> {
            if (!(m instanceof EnumConstantDecl))
                return m;

            EnumConstantDecl cd = (EnumConstantDecl) m;
            Position pos = cd.position();

            // Add the name and ordinal to the constructor call.
            List<Expr> args = new ArrayList<>();
            args.add(nf.StringLit(pos, cd.name().id()).type(ts.String()));
            args.add(nf.IntLit(pos, IntLit.INT, cd.ordinal()).type(ts.Int()));
            args.addAll(cd.args());

            // Declare the field and initializer. We are careful to recycle the enum instance
            // since it is already part of the class type.
            EnumInstance ei = cd.enumInstance();
            Expr init = tnf.New(pos, container, args);
            return nf.FieldDecl(
                    pos, ei.flags(), nf.CanonicalTypeNode(pos, ei.type()),
                    nf.Id(pos, ei.name()), init, /*javaDoc*/ null)
                    .fieldInstance(ei);
        });


        // Add implicitly declared members. These must go directly after the enum
        // constants for correct static initialization order.
        int numConstants = (int) enumDecl.type().fields().stream()
                .filter((fi) -> fi instanceof EnumInstance)
                .count();
        List<ClassMember> members = new ArrayList<>(enumDecl.body().members());
        members.add(numConstants, buildValuesField(enumDecl));
        members.add(numConstants, buildValuesMethod(enumDecl));
        members.add(numConstants, buildValueOfMethod(enumDecl));
        enumDecl = enumDecl.body(enumDecl.body().members(members));

        return enumDecl;
    }

    /** private static final T[] values = {decl1, decl2, ...}; */
    private FieldDecl buildValuesField(ClassDecl enumDecl) {
        Position pos = enumDecl.position();

        // Collect enum constants.
        List<Expr> decls = enumDecl.type().fields().stream()
                .filter((fi) -> fi instanceof EnumInstance)
                .map((fi) -> {
                    EnumInstance ei = (EnumInstance) fi;
                    return tnf.StaticField(pos, ei.name(), enumDecl.type());
                })
                .collect(Collectors.toList());

        // Create field.
        Expr init = nf.ArrayInit(pos, decls).type(ts.arrayOf(enumDecl.type()));
        return tnf.FieldDecl(
                pos, "values", ts.arrayOf(enumDecl.type()), enumDecl.type(), init,
                Flags.NONE.Private().Static().Final());
    }

    /** public static T[] values() { return (T[]) T.values.clone(); } */
    private MethodDecl buildValuesMethod(ClassDecl enumDecl) {
        Position pos = enumDecl.position();

        // Find field.
        Field f = tnf.StaticField(pos, "values", enumDecl.type());

        // Clone, cast, and return
        Call call = tnf.Call(pos, f, "clone", ts.Object(), ts.Object());
        Cast cast = tnf.Cast(pos, ts.arrayOf(enumDecl.type()), call);
        Return ret = nf.Return(pos, cast);

        // Declare method.
        return tnf.MethodDecl(
                pos, "values", enumDecl.type(), ts.arrayOf(enumDecl.type()),
                Collections.emptyList(), nf.Block(pos, ret), Flags.NONE.Public().Static().Final());
    }

    /** public static T valueOf(String s) { return (T) Enum.valueOf(T.class, s); } */
    private MethodDecl buildValueOfMethod(ClassDecl enumDecl) {
        Position pos = enumDecl.position();

        Formal formal = tnf.Formal(pos, "s", ts.String(), Flags.NONE);

        // Call Enum.valueOf(...).
        ClassLit clazz = tnf.ClassLit(pos, enumDecl.type());
        Local s = tnf.Local(pos, formal);
        ClassType container = enumDecl.type().superType().toClass();
        Call call = tnf.StaticCall(pos, "valueOf", container, enumDecl.type(), clazz, s);

        // Cast and return.
        Cast cast = tnf.Cast(pos, enumDecl.type(), call);
        Return ret = nf.Return(pos, cast);

        // Declare method.
        return tnf.MethodDecl(
                pos, "valueOf", enumDecl.type(), enumDecl.type(), Collections.singletonList(formal),
                nf.Block(pos, ret), Flags.NONE.Public().Static().Final());
    }
}
