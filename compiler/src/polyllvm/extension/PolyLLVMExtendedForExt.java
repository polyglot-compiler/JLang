package polyllvm.extension;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.ExtendedFor;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Enhanced for-loops are desugared into normal for-loops. */
public class PolyLLVMExtendedForExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("Enhanced for-loops should have been desugared");
    }

    @Override
    public Node desugar(DesugarLocally v) {
        ExtendedFor n = (ExtendedFor) node();
        try {
            return n.expr().type().isArray()
                ? translateForArray(n, v)
                : translateForIterable(n, v);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    // for (T x: e) { ... }
    // --->
    // for (Iterator<T> it = e.iterator(); it.hasNext(); ) { T x = it.next(); ... }
    private Stmt translateForIterable(ExtendedFor ef, DesugarLocally v) throws SemanticException {
        Position pos = ef.position();
        PolyLLVMTypeSystem ts = v.ts;
        PolyLLVMNodeFactory nf = v.nf;
        TypedNodeFactory tnf = v.tnf;

        assert ef.expr().type().isClass();
        ClassType exprT = ef.expr().type().toClass();
        Type formalT = ef.decl().declType();
        JL5ParsedClassType iterableBaseT = (JL5ParsedClassType) ts.Iterable();
        JL5ParsedClassType iteratorBaseT = (JL5ParsedClassType) ts.Iterator();

        // Build iterator type (read the JLS!).
        ClassType itT;
        JL5SubstClassType iterableGenericT = ts.findGenericSupertype(iterableBaseT, exprT);
        if (iterableGenericT != null) {
            // Instantiate Iterator<T> with the type param of the Iterable<T> we found.
            ReferenceType iterableTParam = iterableGenericT.actuals().get(0);
            itT = ts.instantiate(pos, iteratorBaseT, iterableTParam);
        } else {
            // Raw type.
            itT = ts.rawClass(iteratorBaseT);
        }

        // Build cast type (read the JLS!).
        Type castT;
        if (formalT.isReference()) {
            castT = formalT;
        } else if (iterableGenericT != null) {
            ReferenceType param = iterableGenericT.actuals().get(0);
            castT = ts.applyCaptureConversion(param, pos);
            if (castT instanceof TypeVariable) {
                castT = ((TypeVariable) castT).upperBound();
            }
        } else {
            castT = ts.Object();
        }

        // Initializer: Iterator<T> it = e.iterator()
        Call itCall = tnf.Call(pos, ef.expr(), "iterator", exprT, itT);
        LocalDecl itDecl = tnf.TempSSA("it", itCall);
        List<ForInit> forInit = Collections.singletonList(itDecl);

        // Condition: it.hasNext()
        Local it = tnf.Local(pos, itDecl);
        Call hasNextCall = tnf.Call(pos, copy(it), "hasNext", itT, ts.Boolean());

        // Loop.
        Call nextCall = tnf.Call(pos, copy(it), "next", itT, castT);
        Cast cast = tnf.Cast(nextCall, castT);
        LocalDecl next = ef.decl().init(cast);
        Block body = nf.Block(pos, next, ef.body());
        return nf.For(pos, forInit, hasNextCall, Collections.emptyList(), body);
    }

    // for (T x : e) { ... }
    // --->
    // for (T[] a = e, int i = 0; i < a.length; i++) { T x = a[i]; ... }
    private Stmt translateForArray(ExtendedFor n, DesugarLocally v) {
        Position pos = n.position();
        PolyLLVMTypeSystem ts = v.ts;
        PolyLLVMNodeFactory nf = v.nf;
        TypedNodeFactory tnf = v.tnf;

        Type iteratedT = n.decl().declType();

        // Array alias: T[] a = e;
        LocalDecl aDecl = tnf.TempSSA("a", n.expr());
        Local a = tnf.Local(pos, aDecl);

        // Iterator: int i = 0.
        Expr zero = nf.IntLit(pos, IntLit.INT, 0).type(ts.Int());
        LocalDecl iDecl = tnf.TempVar(pos, "it", ts.Int(), zero);
        Local it = tnf.Local(pos, iDecl);

        // Init.
        List<ForInit> forInit = Arrays.asList(aDecl, iDecl);

        // Condition: i < arr.length
        Field len = tnf.Field(pos, copy(a), "length");
        Expr cond = nf.Binary(pos, copy(it), Binary.LT, len).type(ts.Boolean());

        // Update: i++
        Unary inc = (Unary) nf.Unary(pos, copy(it), Unary.POST_INC).type(ts.Int());
        List<ForUpdate> update = Collections.singletonList(nf.Eval(pos, inc));

        // Loop.
        Expr aAccess = tnf.ArrayAccess(copy(a), copy(it), /*alreadyGuarded*/ true);
        LocalDecl next = n.decl().init(aAccess);
        return nf.For(pos, forInit, cond, update, nf.Block(pos, next, n.body()));
    }
}
