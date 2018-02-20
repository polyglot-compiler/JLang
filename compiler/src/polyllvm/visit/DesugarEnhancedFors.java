package polyllvm.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.ExtendedFor;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Translates enhanced for-loops into normal for-loops (JLS 14.14.2). Derived from
 * {@link polyglot.ext.jl5.visit.RemoveExtendedFors}, but heavily modified for PolyLLVM
 * (e.g., in order to preserve debug information).
 */
public class DesugarEnhancedFors extends DesugarVisitor {
    private int varCount = 0;

    public DesugarEnhancedFors(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    private String freshName(String desc) {
        // We assume that no other visitor creates variable names clashing with these.
        return "extfor$" + desc + "$" + varCount++;
    }

    @Override
    public Node leaveDesugar(Node parent, Node n) throws SemanticException {

        // We must collect labels before translating, so wait until we're at the topmost label.
        if (parent instanceof Labeled)
            return super.leaveDesugar(parent, n);

        // Collect and remove labels (there may be several chained together).
        List<String> labels = new ArrayList<>();
        Node unlabeled = n;
        while (unlabeled instanceof Labeled) {
            Labeled labeled = (Labeled) unlabeled;
            labels.add(labeled.label());
            unlabeled = labeled.statement();
        }

        if (unlabeled instanceof ExtendedFor) {
            ExtendedFor ef = (ExtendedFor) unlabeled;
            if (ef.expr().type().isArray()) {
                return translateForArray(ef, labels);
            } else {
                Stmt loop = translateForIterable(ef);
                return addLabels(loop.position(), loop, labels);
            }
        }

        return super.leaveDesugar(parent, n);
    }

    // L1,...,Ln: for (T x: e) { ... }
    // --->
    // L1,...,Ln: for (Iterator<T> it = e.iterator(); it.hasNext(); ) { T x = it.next(); ... }
    private Stmt translateForIterable(ExtendedFor ef) throws SemanticException {
        Position pos = ef.position();

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
        String itName = freshName("it");
        Call itCall = tnf.Call(pos, ef.expr(), "iterator", exprT, itT);
        LocalDecl itDecl = tnf.LocalDecl(pos, itName, itT, itCall, Flags.NONE);
        List<ForInit> forInit = Collections.singletonList(itDecl);

        // Condition: it.hasNext()
        Local it = tnf.Local(pos, itDecl);
        Call hasNextCall = tnf.Call(pos, copy(it), "hasNext", itT, ts.Boolean());

        // Loop.
        Call nextCall = tnf.Call(pos, copy(it), "next", itT, castT);
        Cast cast = tnf.Cast(pos, castT, nextCall);
        LocalDecl next = ef.decl().init(cast);
        Block body = nf.Block(pos, next, ef.body());
        return nf.For(pos, forInit, hasNextCall, Collections.emptyList(), body);
    }

    // L1,...,Ln: for (T x : e) { ... }
    // --->
    // T[] a = e; L1,...,Ln: for (int i = 0; i < a.length; i++) { T x = a[i]; ... }
    private Stmt translateForArray(ExtendedFor n, List<String> labels) {
        Position pos = n.position();

        Type iteratedT = n.decl().declType();

        // Array alias.
        ReferenceType exprT = n.expr().type().toReference();
        LocalDecl aDecl = tnf.LocalDecl(pos, freshName("arr"), exprT, n.expr(), Flags.FINAL);
        Local a = tnf.Local(pos, aDecl);

        // Initializer: int i = 0
        Expr zero = nf.IntLit(pos, IntLit.INT, 0).type(ts.Int());
        LocalDecl iDecl = tnf.LocalDecl(pos, freshName("it"), ts.Int(), zero, Flags.NONE);
        Local it = tnf.Local(pos, iDecl);
        List<ForInit> forInit = Collections.singletonList(iDecl);

        // Condition: i < arr.length
        Field len = tnf.Field(pos, copy(a), "length", ts.Int(), exprT);
        Expr cond = nf.Binary(pos, copy(it), Binary.LT, len).type(ts.Boolean());

        // Update: i++
        Unary inc = (Unary) nf.Unary(pos, copy(it), Unary.POST_INC).type(ts.Int());
        List<ForUpdate> update = Collections.singletonList(nf.Eval(pos, inc));

        // Loop.
        LocalDecl next = n.decl().init(nf.ArrayAccess(pos, copy(a), copy(it)).type(iteratedT));
        Stmt loop = nf.For(pos, forInit, cond, update, nf.Block(pos, next, n.body()));
        Stmt labeled = addLabels(pos, loop, labels);

        return nf.Block(pos, aDecl, labeled);
    }

    private Stmt addLabels(Position pos, Stmt stmt, List<String> labels) {
        for (int i = labels.size() - 1; i >= 0; --i)
            stmt = nf.Labeled(pos, nf.Id(pos, labels.get(i)), stmt);
        return stmt;
    }

    /** Helper method to help prevent node aliasing. */
    @SuppressWarnings("unchecked")
    private static <T> T copy(Node n) {
        return (T) n.copy();
    }
}
