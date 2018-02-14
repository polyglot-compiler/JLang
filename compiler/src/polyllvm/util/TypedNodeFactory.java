package polyllvm.util;

import polyglot.ast.*;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.*;
import polyglot.util.Position;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper methods for creating typed(!) nodes.
 * Handles generics.
 *
 * This is used in PolyLLVM, so we don't care about the following.
 * - exception types on method instances
 * - javadoc nodes
 */
public class TypedNodeFactory {
    protected final JL5TypeSystem ts;
    protected final NodeFactory nf;

    public TypedNodeFactory(JL5TypeSystem ts, NodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Formals and variables.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public FieldDecl FieldDecl(
            Position pos, String name, Type type, ReferenceType container, Expr init, Flags flags) {
        return nf.FieldDecl(pos, flags, nf.CanonicalTypeNode(pos, type), nf.Id(pos, name), init)
                .fieldInstance(ts.fieldInstance(pos, container, flags, type, name));
    }

    public Field Field(
            Position pos, String name, Type type, ReferenceType container, Flags flags) {
        return (Field) nf.Field(pos, nf.CanonicalTypeNode(pos, container), nf.Id(pos, name))
                .fieldInstance(ts.fieldInstance(pos, container, flags, type, name))
                .type(type);
    }

    public Formal Formal(Position pos, String name, Type type, Flags flags) {
        return nf.Formal(pos, flags, nf.CanonicalTypeNode(pos, type), nf.Id(pos, name))
                .localInstance(ts.localInstance(pos, flags, type, name));
    }

    public LocalDecl LocalDecl(Position pos, String name, Type type, Flags flags) {
        return nf.LocalDecl(pos, flags, nf.CanonicalTypeNode(pos, type), nf.Id(pos, name))
                .localInstance(ts.localInstance(pos, flags, type, name));
    }

    public Local Local(Position pos, String name, Type type, Flags flags) {
        return (Local) nf.Local(pos, nf.Id(pos, name))
                .localInstance(ts.localInstance(pos, flags, type, name))
                .type(type);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods and constructors.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public MethodDecl MethodDecl(
            Position pos, String name, ReferenceType container, Type returnType,
            List<Formal> formals, Block body, Flags flags) {
        List<Type> argTypes = formals.stream().map(Formal::declType).collect(Collectors.toList());
        return nf.MethodDecl(
                pos, flags, nf.CanonicalTypeNode(pos, returnType), nf.Id(pos, name), formals,
                Collections.emptyList(), // PolyLLVM does not care about exn types.
                body, /*javaDoc*/ null)
                .methodInstance(ts.methodInstance(
                        pos, container, flags, returnType, name, argTypes,
                        Collections.emptyList())); // PolyLLVM does not care about exn types.
    }

    public Call StaticCall(
            Position pos, String name, ReferenceType container, Type returnType,
            Flags flags, Expr... args) {
        return Call(
                pos, nf.CanonicalTypeNode(pos, container), name,
                container, returnType, flags, args);
    }

    public Call Call(
            Position pos, Receiver receiver, String name, ReferenceType container, Type returnType,
            Flags flags, Expr... args) {
        List<Type> argTypes = Arrays.stream(args).map(Expr::type).collect(Collectors.toList());
        JL5MethodInstance mi = (JL5MethodInstance) ts.methodInstance(
                pos, container, flags, returnType, name, argTypes, Collections.emptyList());
        mi = ts.methodCallValid(mi, mi.name(), argTypes, /*actualTypeArgs*/ null, returnType);
        assert mi != null;
        return (Call) nf.Call(pos, receiver, nf.Id(pos, name), args)
                .methodInstance(mi)
                .type(returnType);
    }

    public ConstructorCall ConstructorCall(
            Position pos, ConstructorCall.Kind kind, ClassType container,
            Flags flags, Expr... args) {
        List<Type> argTypes = Arrays.stream(args).map(Expr::type).collect(Collectors.toList());
        return nf.ConstructorCall(pos, kind, Arrays.asList(args))
                .constructorInstance(ts.constructorInstance(
                        pos, container, flags, argTypes,
                        Collections.emptyList())); // PolyLLVM does not care about exn types.
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public Cast Cast(Position pos, Type type, Expr expr) {
        return (Cast) nf.Cast(pos, nf.CanonicalTypeNode(pos, type), expr).type(type);
    }

    public ClassLit ClassLit(Position pos, Type type) {
        return (ClassLit) nf.ClassLit(pos, nf.CanonicalTypeNode(pos, type)).type(ts.Class());
    }
}
