//Copyright (C) 2017 Cornell University

package jlang.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.util.Position;

import java.util.*;
import java.util.stream.Collectors;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;

/**
 * Desugar Java enum classes into normal classes. Derived from
 * {@link polyglot.ext.jl5.visit.RemoveEnums}, but heavily modified for JLang.
 * Preserves typing.
 */
public class DesugarEnums extends DesugarVisitor {

    public DesugarEnums(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveDesugar(Node parent, Node n) {

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
    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        if (!JL5Flags.isEnum(ct.flags()))
            return cb;

        // Prepend name and ordinal to each constructor.
        List<Formal> extraFormals = Arrays.asList(
                tnf.Formal(cb.position(), "enum$name", ts.String(), Flags.FINAL),
                tnf.Formal(cb.position(), "enum$ordinal", ts.Int(), Flags.FINAL));
        cb = prependConstructorFormals(ct, cb, extraFormals);

        // Include name and ordinal in super constructor calls.
        ClassType enumClass = ct.toClass().superType().toClass();
        cb = mapConstructors(cb, (ctor) -> {
            LinkedList<Stmt> stmts = new LinkedList<>(ctor.body().statements());
            ConstructorCall oldCC = stmts.peek() instanceof ConstructorCall
                    ? (ConstructorCall) stmts.pop()
                    : null;

            if (oldCC == null || oldCC.kind().equals(ConstructorCall.SUPER)) {
                // Create super constructor call with name and ordinal as arguments.
                List<Expr> locals = ctor.formals().stream()
                        .limit(extraFormals.size())
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
        // Updates to constructors must precede this.
        cb = mapMembers(cb, (m) -> {
            if (!(m instanceof EnumConstantDecl))
                return m;

            EnumConstantDecl cd = (EnumConstantDecl) m;
            Position pos = cd.position();

            // If there is an anonymous class here, update its super constructor calls.
            if (cd.body() != null) {
                ClassBody body = cd.body();
                ParsedClassType anonType = cd.type();
                body = prependConstructorFormals(anonType, body, extraFormals);
                body = mapConstructors(body, (ctor) ->
                    mapConstructorCall(ctor, (cc) -> {
                        assert cc.kind().equals(ConstructorCall.SUPER);
                        List<Expr> extraArgs = ctor.formals().stream()
                                .limit(extraFormals.size())
                                .map((f) -> tnf.Local(f.position(), f))
                                .collect(Collectors.toList());
                        List<Expr> args = concat(extraArgs, cc.arguments());
                        return tnf.ConstructorCall(cc.position(), cc.kind(), ct, args);
                    })
                );
                cd = cd.body(body);
            }

            // Add the name and ordinal to the constructor call.
            List<Expr> args = new ArrayList<>();
            args.add(tnf.StringLit(pos, cd.name().id()));
            args.add(nf.IntLit(pos, IntLit.INT, cd.ordinal()).type(ts.Int()));
            args.addAll(cd.args());

            // Declare the field and initializer. We are careful to recycle the enum instance
            // since it is already part of the class type.
            EnumInstance ei = cd.enumInstance();
            Expr init = tnf.New(pos, cd.type(), /*outer*/ null, args, cd.body());
            return nf.FieldDecl(
                    pos, ei.flags(), nf.CanonicalTypeNode(pos, ei.type()),
                    nf.Id(pos, ei.name()), init, /*javaDoc*/ null)
                    .fieldInstance(ei);
        });


        // Add implicitly declared members. These must go directly after the enum
        // constants for correct static initialization order.
        int numConstants = (int) ct.fields().stream()
                .filter((fi) -> fi instanceof EnumInstance)
                .count();
        List<ClassMember> members = new ArrayList<>(cb.members());
        members.add(numConstants, buildValuesField(ct));
        members.add(numConstants, buildValuesMethod(ct));
        members.add(numConstants, buildValueOfMethod(ct));
        cb = cb.members(members);

        return super.leaveClassBody(ct, cb);
    }

    /** private static final T[] values = {decl1, decl2, ...}; */
    private FieldDecl buildValuesField(ParsedClassType enumType) {
        Position pos = enumType.position();

        // Collect enum constants.
        List<Expr> decls = enumType.fields().stream()
                .filter((fi) -> fi instanceof EnumInstance)
                .map((fi) -> {
                    EnumInstance ei = (EnumInstance) fi;
                    return tnf.StaticField(pos, enumType, ei.name());
                })
                .collect(Collectors.toList());

        // Create field.
        Expr init = nf.ArrayInit(pos, decls).type(ts.arrayOf(enumType));
        return tnf.FieldDecl(
                pos, enumType, Flags.NONE.Private().Static().Final(),
                ts.arrayOf(enumType), "values", init
        );
    }

    /** public static T[] values() { return (T[]) T.values.clone(); } */
    private MethodDecl buildValuesMethod(ParsedClassType enumType) {
        Position pos = enumType.position();

        // Find field.
        Field f = tnf.StaticField(pos, enumType, "values");

        // Clone, cast, and return
        Call call = tnf.Call(pos, f, "clone", ts.Object(), ts.Object());
        Cast cast = tnf.Cast(call, ts.arrayOf(enumType));
        Return ret = nf.Return(pos, cast);

        // Declare method.
        return tnf.MethodDecl(
                pos, enumType, Flags.NONE.Public().Static().Final(),
                ts.arrayOf(enumType), "values", Collections.emptyList(),
                nf.Block(pos, ret));
    }

    /** public static T valueOf(String s) { return (T) Enum.valueOf(T.class, s); } */
    private MethodDecl buildValueOfMethod(ParsedClassType enumType) {
        Position pos = enumType.position();

        Formal formal = tnf.Formal(pos, "s", ts.String(), Flags.FINAL);

        // Call Enum.valueOf(...).
        ClassLit clazz = tnf.ClassLit(pos, enumType);
        Local s = tnf.Local(pos, formal);
        ClassType container = enumType.superType().toClass();
        Call call = tnf.StaticCall(pos, container, enumType, "valueOf", clazz, s);

        // Cast and return.
        Cast cast = tnf.Cast(call, enumType);
        Return ret = nf.Return(pos, cast);

        // Declare method.
        return tnf.MethodDecl(
                pos, enumType, Flags.NONE.Public().Static().Final(),
                enumType, "valueOf", Collections.singletonList(formal),
                nf.Block(pos, ret));
    }
}
