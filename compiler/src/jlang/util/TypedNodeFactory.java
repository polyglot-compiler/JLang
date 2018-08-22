package jlang.util;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jlang.ast.*;
import jlang.extension.JLangArrayAccessExt;
import jlang.extension.JLangCallExt;
import jlang.types.JLangTypeSystem;

/**
 * Helper methods for creating typed nodes. Handles generics.
 *
 * This is used in JLang, so we don't care about (for example)
 * exception types on method instances.
 */
public class TypedNodeFactory {
    protected final JLangTypeSystem ts;
    protected final JLangNodeFactory nf;

    public TypedNodeFactory(JLangTypeSystem ts, JLangNodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fields, formals, and variables.
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a field declaration, adding a corresponding field instance to
     * {@code container} if it does not already exist.
     */
    public FieldDecl FieldDecl(
            Position pos, ParsedClassType container,
            Flags flags, Type type, String name, Expr init) {
        FieldInstance fi = ts.fieldInstance(pos, container, flags, type, name);
        container.addField(fi);
        CanonicalTypeNode typeNode = nf.CanonicalTypeNode(pos, type);
        Id id = nf.Id(pos, name);
        return nf.FieldDecl(pos, flags, typeNode, id, init).fieldInstance(fi);
    }

    /**
     * Like {@link TypedNodeFactory#StaticField(Position, ReferenceType, String)},
     * but does not assume that the static field already exists in {@code container}.
     */
    public Field StaticFieldForced(
            Position pos, ReferenceType container, Flags flags, Type type, String name) {
        FieldInstance fi = ts.fieldInstance(pos, container, flags, type, name);
        CanonicalTypeNode receiver = nf.CanonicalTypeNode(pos, container);
        return Field(pos, receiver, fi);
    }

    public Field StaticField(Position pos, ReferenceType container, String name) {
        CanonicalTypeNode receiver = nf.CanonicalTypeNode(pos, container);
        return Field(pos, receiver, name);
    }

    public Field Field(Position pos, Receiver receiver, String name) {
        // We lie and tell the type system that fromClass == container since we want to bypass
        // visibility checks. If container is not a class type, we instead use Object.
        ReferenceType container = receiver.type().toReference();
        ClassType fromClass = container.isClass() ? container.toClass() : ts.Object();
        try {
            FieldInstance fi = ts.findField(container, name, fromClass, /*fromClient*/ true);
            return Field(pos, receiver, fi);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    private Field Field(Position pos, Receiver receiver, FieldInstance fi) {
        Id id = nf.Id(pos, fi.name());
        return (Field) nf.Field(pos, receiver, id).fieldInstance(fi).type(fi.type());
    }

    public Formal Formal(Position pos, String name, Type type, Flags flags) {
        TypeNode typeNode = nf.CanonicalTypeNode(pos, type);
        Id id = nf.Id(pos, name);
        LocalInstance li = ts.localInstance(pos, flags, type, name);
        return nf.Formal(pos, flags, typeNode, id).localInstance(li);
    }

    public LocalDecl TempVar(Position pos, String name, Type type, Expr init) {
        return Temp(pos, name, type, init, Flags.NONE, /*isSSA*/ false);
    }

    public LocalDecl TempSSA(String name, Expr init) {
        if (init == null)
            throw new InternalCompilerError("SSA temporaries must have an init expression");
        return Temp(init.position(), name, init.type(), init, Flags.FINAL, /*isSSA*/ true);
    }

    private LocalDecl Temp(
            Position pos, String name, Type type, Expr init, Flags flags, boolean isSSA) {
        return nf.LocalDecl(pos, flags, nf.CanonicalTypeNode(pos, type), nf.Id(pos, name), init)
                .localInstance(ts.localInstance(pos, flags, type, name, /*isTemp*/ true, isSSA));
    }

    public Local Local(Position pos, VarDecl vd) {
        return Local(pos, vd.localInstance());
    }

    public Local Local(Position pos, LocalInstance li) {
        return (Local) nf.Local(pos, nf.Id(pos, li.name())).localInstance(li).type(li.type());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods and constructors.
    ////////////////////////////////////////////////////////////////////////////

    public MethodDecl MethodDecl(
            Position pos, ParsedClassType container, Flags flags, Type returnType, String name,
            List<Formal> formals, Block body) {
        List<Type> argTypes = formals.stream().map(Formal::declType).collect(Collectors.toList());
        MethodInstance mi = ts.methodInstance(
                pos, container, flags, returnType, name, argTypes,
                Collections.emptyList()); // JLang does not care about exn types.
        container.addMethod(mi);
        TypeNode returnTypeNode = nf.CanonicalTypeNode(pos, returnType);
        Id id = nf.Id(pos, name);
        return nf.MethodDecl(
                pos, flags, returnTypeNode, id, formals,
                Collections.emptyList(), // JLang does not care about exn types.
                body, /*javaDoc*/ null)
                .methodInstance(mi);
    }

    public ConstructorDecl ConstructorDecl(
            Position pos, ParsedClassType container, List<Formal> formals, Block body) {
        List<Type> argTypes = formals.stream().map(Formal::declType).collect(Collectors.toList());
        Flags flags = Flags.NONE; // Constructor flags not important for JLang.
        ConstructorInstance ci = ts.constructorInstance(
                pos, container, flags, argTypes,
                Collections.emptyList()); // JLang does not care about exn types.
        container.addConstructor(ci);
        return nf.ConstructorDecl(
                pos, flags, nf.Id(pos, container.name()),
                formals, Collections.emptyList(), body, /*javaDoc*/ null)
                .constructorInstance(ci);
    }

    /**
     * Like {@link TypedNodeFactory#StaticCall(Position, ClassType, Type, String, Expr...)},
     * but does not assume that the static method already exists in {@code container}.
     */
    public Call StaticCallForced(
            Position pos, ReferenceType container,
            Flags flags, Type returnType, String name, Expr... args) {
        List<Type> argTypes = Arrays.stream(args).map(Expr::type).collect(Collectors.toList());
        MethodInstance mi = ts.methodInstance(
                pos, container, flags, returnType, name, argTypes,
                Collections.emptyList()); // JLang does not care about exn types.
        Receiver receiver = nf.CanonicalTypeNode(pos, container);
        return Call(pos, receiver, mi, args);
    }

    public Call StaticCall(
            Position pos, ClassType container, Type returnType, String name, Expr... args) {
        Receiver receiver = nf.CanonicalTypeNode(pos, container);
        return Call(pos, receiver, name, container, returnType, args);
    }

    public Call Call(
            Position pos, Receiver receiver, String name, ClassType container,
            Type returnType, Expr... args) {
        List<Type> argTypes = Arrays.stream(args).map(Expr::type).collect(Collectors.toList());
        try {
            MethodInstance mi = ts.findMethod(
                    container, name, argTypes, /*actualTypeArgs*/ null, container,
                    returnType, /*fromClient*/ true);
            return Call(pos, receiver, mi, args);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    private Call Call(Position pos, Receiver receiver, MethodInstance mi, Expr... args) {
        Id id = nf.Id(pos, mi.name());
        Call c = (Call) nf.Call(pos, receiver, id, args)
                .methodInstance(mi)
                .type(mi.returnType());
        JLangCallExt ext = (JLangCallExt) JLangExt.ext(c);
        return ext.determineIfDirect(c);
    }

    public ConstructorCall ConstructorCall(
            Position pos, ConstructorCall.Kind kind, ClassType container, List<Expr> args) {
        List<Type> argTypes = args.stream().map(Expr::type).collect(Collectors.toList());
        try {
            ConstructorInstance ci = ts.findConstructor(
                    container, argTypes, /*actualTypeArgs*/ null, container, /*fromClient*/ true);
            return nf.ConstructorCall(pos, kind, args).constructorInstance(ci);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    /**
     * Assumes that {@link jlang.visit.DeclareExplicitAnonCtors} has already declared
     * explicit constructors for anonymous classes.
     */
    public New New(Position pos, ClassType type, Expr outer, List<Expr> args, ClassBody body) {
        List<Type> argTypes = args.stream().map(Expr::type).collect(Collectors.toList());
        try {
            ConstructorInstance ci = ts.findConstructor(
                    type, argTypes, /*actualTypeArgs*/ null, type, /*fromClient*/ true);
            New res = (New) nf.New(pos, outer, nf.CanonicalTypeNode(pos, type), args, body)
                    .constructorInstance(ci)
                    .type(type);
            if (body != null) {
                if (!(type instanceof ParsedClassType))
                    throw new InternalCompilerError(
                            "Trying to create new anonymous instance without parsed class type");
                res = res.anonType((ParsedClassType) type);
            }
            return res;

        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc
    ////////////////////////////////////////////////////////////////////////////

    public AddressOf AddressOf(Expr expr) {
        return (AddressOf) nf.AddressOf(expr.position(), expr).type(expr.type());
    }

    public Load Load(Expr expr) {
        return (Load) nf.Load(expr.position(), expr).type(expr.type());
    }

    public Cast Cast(Expr expr, Type type) {
        Position pos = expr.position();
        return (Cast) nf.Cast(pos, nf.CanonicalTypeNode(pos, type), expr).type(type);
    }

    public ClassLit ClassLit(Position pos, ReferenceType type) {
        Type classType = ts.Class(pos, type);
        return (ClassLit) nf.ClassLit(pos, nf.CanonicalTypeNode(pos, type)).type(classType);
    }

    public Eval EvalAssign(Expr target, Expr val) {
        Position pos = target.position();
        Assign assign = (Assign) nf.Assign(pos, target, Assign.ASSIGN, val).type(target.type());
        return nf.Eval(pos, assign);
    }

    public Special This(Position pos, ReferenceType container) {
        return (Special) nf.This(pos, nf.CanonicalTypeNode(pos, container)).type(container);
    }

    public Special UnqualifiedThis(Position pos, ReferenceType container) {
        return (Special) nf.This(pos).type(container);
    }

    public Instanceof InstanceOf(Expr expr, ReferenceType type) {
        Position pos = expr.position();
        CanonicalTypeNode typeNode = nf.CanonicalTypeNode(pos, type);
        return (Instanceof) nf.Instanceof(pos, expr, typeNode).type(ts.Boolean());
    }

    public If If(Expr cond, Stmt consequent) {
        assert cond.type().typeEquals(ts.Boolean());
        return nf.If(cond.position(), cond, consequent);
    }

    public ESeq ESeq(List<Stmt> statements, Expr expr) {
        return (ESeq) nf.ESeq(expr.position(), statements, expr).type(expr.type());
    }

    public Throw Throw(Position pos, ClassType t, List<Expr> args) {
        assert t.isSubtype(ts.Throwable());
        New exn = New(pos, t, /*outer*/ null, args, /*body*/ null);
        return nf.Throw(pos, exn);
    }

    public Unary Not(Expr expr) {
        assert expr.type().typeEquals(ts.Boolean());
        return (Unary) nf.Unary(expr.position(), Unary.NOT, expr).type(ts.Boolean());
    }

    public Binary CondOr(Expr l, Expr r) {
        assert l.type().typeEquals(ts.Boolean());
        assert r.type().typeEquals(ts.Boolean());
        return (Binary) nf.Binary(l.position(), l, Binary.COND_OR, r).type(ts.Boolean());
    }

    public Binary CondAnd(Expr l, Expr r) {
    	assert l.type().typeEquals(ts.Boolean());
        assert r.type().typeEquals(ts.Boolean());
        return (Binary) nf.Binary(l.position(), l, Binary.COND_AND, r).type(ts.Boolean());
    }

    public ArrayAccess ArrayAccess(Expr base, Expr index, boolean alreadyGuarded) {
        assert base.type().isArray();
        ArrayAccess n = (ArrayAccess) nf.ArrayAccess(base.position(), base, index)
                .type(base.type().toArray().base());
        if (alreadyGuarded)
            n = ((JLangArrayAccessExt) JLangExt.ext(n)).setGuarded();
        return n;
    }

    public Type typeForName(String name) {
        try {
            return ts.typeForName(name);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    public StringLit StringLit(Position pos, String value) {
        return (StringLit) nf.StringLit(pos, value).type(ts.String());
    }

    public NullLit NullLit(Position pos) {
        return (NullLit) nf.NullLit(pos).type(ts.Null());
    }

    public Binary IsNull(Expr e) {
    	return (Binary) nf.Binary(e.position(), e, Binary.EQ, NullLit(e.position())).type(ts.Boolean());
    }
}
