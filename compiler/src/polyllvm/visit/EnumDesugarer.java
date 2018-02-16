package polyllvm.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.util.TypedNodeFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Desugar Java enum classes into normal classes. Derived from
 * {@link polyglot.ext.jl5.visit.RemoveEnums}, but heavily modified for PolyLLVM.
 * Preserves typing.
 */
public class EnumDesugarer extends NodeVisitor {

    protected JL5TypeSystem ts;
    protected JL5NodeFactory nf;
    private TypedNodeFactory tnf;

    public EnumDesugarer(JL5TypeSystem ts, JL5NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        this.tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {

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

        // Mutate constructor instances to include arguments for the enum name and ordinal.
        // This ensures that any constructor calls we create later will resolve correctly.
        enumDecl.type().setConstructors(
                enumDecl.type().constructors().stream()
                        .map((ci) -> {
                            List<Type> formals = new ArrayList<>();
                            formals.add(ts.String()); // Name.
                            formals.add(ts.Int()); // Ordinal.
                            formals.addAll(ci.formalTypes());
                            return updateFormals(ci, formals);
                        })
                        .collect(Collectors.toList()));

        List<ClassMember> members = new ArrayList<>(enumDecl.body().members());

        // Translate constructors and enum constants.
        List<FieldDecl> enumConstants = new ArrayList<>();
        for (int i = 0; i < members.size(); ++i) {
            ClassMember m = members.get(i);
            if (m instanceof ConstructorDecl)
                m = translateEnumConstructor((ConstructorDecl) m, enumDecl);
            if (m instanceof EnumConstantDecl) {
                m = translateEnumConstantDecl((EnumConstantDecl) m, enumDecl);
                enumConstants.add((FieldDecl) m);
            }
            members.set(i, m);
        }

        // Add implicitly declared members. These must go directly after the enum
        // constants for correct static initialization order.
        // Recall that enum constants are required to be at the beginning of the class body.
        int numConstants = enumConstants.size();
        members.add(numConstants, buildValuesField(enumDecl));
        members.add(numConstants, buildValuesMethod(enumDecl));
        members.add(numConstants, buildValueOfMethod(enumDecl));

        // Update class body.
        enumDecl = enumDecl.body(enumDecl.body().members(members));

        return enumDecl;
    }

    /** Adds two new arguments for the name and ordinal, and hands those to the Enum super class. */
    private ConstructorDecl translateEnumConstructor(ConstructorDecl n, ClassDecl enumDecl) {
        Position pos = n.position();
        String name = "enum$name";
        String ordinal = "enum$ordinal";

        // Add two new formals to the constructor declaration.
        List<Formal> formals = new ArrayList<>();
        formals.add(tnf.Formal(pos, name, ts.String(), Flags.NONE));
        formals.add(tnf.Formal(pos, ordinal, ts.Int(), Flags.NONE));
        formals.addAll(n.formals());
        n = (ConstructorDecl) n.formals(formals);

        // Update the associated constructor instance.
        // Note that the constructor instance in the enum type should already have been updated.
        List<Type> formTypes = formals.stream().map(Formal::declType).collect(Collectors.toList());
        n = n.constructorInstance(updateFormals(n.constructorInstance(), formTypes));

        // Supply the constructor call with the new arguments.
        ConstructorCall.Kind kind = ConstructorCall.SUPER;
        List<Expr> args = new ArrayList<>();
        args.add(tnf.Local(pos, name, ts.String(), Flags.NONE));
        args.add(tnf.Local(pos, ordinal, ts.Int(), Flags.NONE));

        // If there is already a constructor call, subsume it.
        LinkedList<Stmt> oldStmts = new LinkedList<>(n.body().statements());
        if (oldStmts.peek() instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) oldStmts.remove();
            if (cc.kind() == ConstructorCall.THIS) {
                kind = cc.kind();
                args.addAll(cc.arguments());
            } else {
                // PolyLLVM seems to insert phony calls to super(null, 0), so we just remove it.
            }
        }

        ClassType container = kind == ConstructorCall.THIS
                ? enumDecl.type()
                : enumDecl.type().superType().toClass();
        ConstructorCall cc = tnf.ConstructorCall(
                n.body().position(), kind, container, args.toArray(new Expr[args.size()]));

        // Add the constructor call to the body.
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(cc);
        stmts.addAll(oldStmts);
        n = (ConstructorDecl) n.body(n.body().statements(stmts));

        return n;
    }

    // Convert an enum constant to a static field.
    private FieldDecl translateEnumConstantDecl(EnumConstantDecl n, ClassDecl enumDecl) {
        Position pos = n.position();

        // Add the name and ordinal to the constructor call.
        List<Expr> args = new ArrayList<>();
        args.add(nf.StringLit(pos, n.name().id()).type(ts.String()));
        args.add(nf.IntLit(pos, IntLit.INT, n.ordinal()).type(ts.Int()));
        args.addAll(n.args());

        // Declare the field and initializer; recycle the enum instance.
        EnumInstance ei = n.enumInstance();
        Expr init = tnf.New(pos, enumDecl.type(), args.toArray(new Expr[args.size()]));
        return nf.FieldDecl(
                pos, ei.flags(), nf.CanonicalTypeNode(pos, ei.type()),
                nf.Id(pos, ei.name()), init, /*javaDoc*/ null)
                .fieldInstance(ei);
    }

    /** private static final T[] values = {decl1, decl2, ...}; */
    private FieldDecl buildValuesField(ClassDecl enumDecl) {
        Position pos = enumDecl.position();

        // Collect enum constants.
        List<Expr> decls = enumDecl.type().fields().stream()
                .filter((fi) -> fi instanceof EnumInstance)
                .map((fi) -> {
                    EnumInstance ei = (EnumInstance) fi;
                    return tnf.Field(pos, ei.name(), ei.type(), enumDecl.type());
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
        Field f = tnf.Field(pos, "values", ts.arrayOf(enumDecl.type()), enumDecl.type());

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

        // Call Enum.valueOf(...).
        ClassLit clazz = tnf.ClassLit(pos, enumDecl.type());
        Local arg = tnf.Local(pos, "s", ts.String(), Flags.NONE);
        ClassType container = enumDecl.type().superType().toClass();
        Call call = tnf.StaticCall(pos, "valueOf", container, enumDecl.type(), clazz, arg);

        // Cast and return.
        Cast cast = tnf.Cast(pos, enumDecl.type(), call);
        Return ret = nf.Return(pos, cast);

        // Declare method.
        Formal formal = tnf.Formal(pos, "s", ts.String(), Flags.NONE);
        return tnf.MethodDecl(
                pos, "valueOf", enumDecl.type(), enumDecl.type(), Collections.singletonList(formal),
                nf.Block(pos, ret), Flags.NONE.Public().Static().Final());
    }

    private ConstructorInstance updateFormals(ConstructorInstance ci, List<Type> formalTypes) {
        // We are careful to update the "original" constructor instance as well.
        ci = ci.formalTypes(formalTypes);
        ci.setDeclaration(ci.orig().formalTypes(formalTypes));
        return ci;
    }
}
