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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desugar Java enum classes into normal classes. Derived from
 * {@link polyglot.ext.jl5.visit.RemoveEnums}, but heavily modified for PolyLLVM.
 * Preserves typing.
 */
public class EnumVisitor extends NodeVisitor {

    protected JL5TypeSystem ts;
    protected JL5NodeFactory nf;
    private TypedNodeFactory tnf;

    public EnumVisitor(JL5TypeSystem ts, JL5NodeFactory nf) {
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
        return tnf.Call(
                expr.position(), expr, "ordinal", ts.Enum(), ts.Int(),
                Flags.NONE.Public().Final());
    }

    /** Convert enum declaration to class declaration. */
    private Node translateEnumDecl(ClassDecl enumDecl) {

        // Set super class to java.lang.Enum.
        enumDecl = enumDecl.superClass(nf.CanonicalTypeNode(enumDecl.position(), ts.Enum()));

        // Translate constructor and constants.
        List<FieldDecl> enumConstants = new ArrayList<>();
        List<ClassMember> otherMembers = new ArrayList<>();
        for (ClassMember m : enumDecl.body().members()) {
            if (m instanceof EnumConstantDecl) {
                enumConstants.add(translateEnumConstantDecl((EnumConstantDecl) m, enumDecl));
            } else if (m instanceof ConstructorDecl) {
                otherMembers.add(translateEnumConstructor((ConstructorDecl) m));
            } else {
                otherMembers.add(m);
            }
        }

        // Add implicitly declared members. These must go directly after the enum
        // constants for correct static initialization order.
        List<ClassMember> members = new ArrayList<>(enumConstants);
        members.add(buildValuesField(enumDecl));
        members.add(buildValuesMethod(enumDecl));
        members.add(buildValueOfMethod(enumDecl));
        members.addAll(otherMembers);
        enumDecl = enumDecl.body(enumDecl.body().members(members));

        // Update class type.
        List<FieldInstance> fields = enumDecl.body().members().stream()
                .filter((m) -> m instanceof FieldDecl)
                .map((m) -> ((FieldDecl) m).fieldInstance())
                .collect(Collectors.toList());
        List<ConstructorInstance> constructors = enumDecl.body().members().stream()
                .filter((m) -> m instanceof ConstructorDecl)
                .map((m) -> ((ConstructorDecl) m).constructorInstance())
                .collect(Collectors.toList());
        List<MethodInstance> methods = enumDecl.body().members().stream()
                .filter((m) -> m instanceof MethodDecl)
                .map((m) -> ((MethodDecl) m).methodInstance())
                .collect(Collectors.toList());
        enumDecl.type().setFields(fields);
        enumDecl.type().setConstructors(constructors);
        enumDecl.type().setMethods(methods);

        return enumDecl;
    }

    /** Adds two new arguments for the name and ordinal, and hands those to the Enum super class. */
    private ConstructorDecl translateEnumConstructor(ConstructorDecl n) {
        Position pos = n.position();
        String name = "enum$name";
        String ordinal = "enum$ordinal";

        // Add two new formals to the constructor declaration.
        List<Formal> formals = new ArrayList<>();
        formals.add(tnf.Formal(pos, name, ts.String(), Flags.NONE));
        formals.add(tnf.Formal(pos, ordinal, ts.Int(), Flags.NONE));
        formals.addAll(n.formals());
        n = (ConstructorDecl) n.formals(formals);

        // Update the constructor instance.
        List<Type> formalTypes = formals.stream()
                .map(VarDecl::declType)
                .collect(Collectors.toList());
        n = n.constructorInstance(updateFormals(n.constructorInstance(), formalTypes));

        // Extract the old constructor call (or create one).
        // Polyglot seems to insert phony calls to super(null, 0) at the beginning of some
        // enum constructors, so we handle that case as well.
        LinkedList<Stmt> oldStmts = new LinkedList<>(n.body().statements());
        ConstructorCall oldCC = null;
        if (oldStmts.peek() instanceof ConstructorCall)
            oldCC = (ConstructorCall) oldStmts.remove(0);
        if (oldCC == null || oldCC.kind() == ConstructorCall.SUPER)
            oldCC = tnf.ConstructorCall(pos, ConstructorCall.SUPER, ts.Enum(), Flags.PROTECTED);

        // Supply the arguments in the constructor call.
        List<Expr> args = new ArrayList<>();
        args.add(tnf.Local(pos, name, ts.String(), Flags.NONE));
        args.add(tnf.Local(pos, ordinal, ts.Int(), Flags.NONE));
        args.addAll(oldCC.arguments());
        oldCC = (ConstructorCall) oldCC.arguments(args);

        // Update the constructor call instance.
        List<Type> argTypes = args.stream().map(Expr::type).collect(Collectors.toList());
        oldCC = oldCC.constructorInstance(updateFormals(oldCC.constructorInstance(), argTypes));

        // Add the constructor call to the body.
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(oldCC);
        stmts.addAll(oldStmts);
        n = (ConstructorDecl) n.body(n.body().statements(stmts));

        return n;
    }

    // Convert an enum constant to a static field.
    private FieldDecl translateEnumConstantDecl(EnumConstantDecl n, ClassDecl enumDecl) {
        Position pos = n.position();

        // Add the name and ordinal to the constructor instance.
        LinkedList<Expr> args = new LinkedList<>();
        args.add(nf.StringLit(pos, n.name().id()).type(ts.String()));
        args.add(nf.IntLit(pos, IntLit.INT, n.ordinal()).type(ts.Int()));
        args.addAll(n.args());
        List<Type> argTypes = args.stream().map(Expr::type).collect(Collectors.toList());
        ConstructorInstance ci = updateFormals(n.constructorInstance(), argTypes);

        // Initialize the field.
        Expr init = nf.New(pos, nf.CanonicalTypeNode(pos, enumDecl.type()), args, n.body())
                .constructorInstance(ci)
                .type(ci.container().toType());

        // Declare the field; recycle the enum instance.
        EnumInstance ei = n.enumInstance();
        return tnf.FieldDecl(pos, ei.name(), enumDecl.type(), enumDecl.type(), init, ei.flags());
    }

    /** private static final T[] values = {decl1, decl2, ...}; */
    private FieldDecl buildValuesField(ClassDecl enumDecl) {
        Position pos = enumDecl.position();

        // Collect enum constants.
        List<Expr> decls = enumDecl.type().members().stream()
                .filter((mi) -> mi instanceof EnumInstance)
                .map((mi) -> {
                    EnumInstance ei = (EnumInstance) mi;
                    return tnf.Field(pos, ei.name(), ei.type(), enumDecl.type(), ei.flags());
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
        Field f = tnf.Field(
                pos, "values", ts.arrayOf(enumDecl.type()), enumDecl.type(),
                Flags.NONE.Private().Static().Final());

        // Clone, cast, and return
        Call call = tnf.Call(pos, f, "clone", ts.Object(), ts.Object(), Flags.PROTECTED);
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
        Local arg = tnf.Local(pos, "s", ts.String(), Flags.NONE);
        ClassLit clazz = tnf.ClassLit(pos, enumDecl.type());
        Call call = tnf.StaticCall(
                pos, "valueOf", ts.Enum(), enumDecl.type(),
                Flags.NONE.Public().Static(), clazz, arg);


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
