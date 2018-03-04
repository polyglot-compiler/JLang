package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.util.Arrays;
import java.util.Collections;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** Whether this array access is already guarded by an index bounds check. */
    private boolean guarded = false;

    public ArrayAccess setGuarded(ArrayAccess c) {
        PolyLLVMArrayAccessExt ext = (PolyLLVMArrayAccessExt) PolyLLVMExt.ext(c);
        if (ext.guarded) return c;
        if (c == node) {
            c = Copy.Util.copy(c);
            ext = (PolyLLVMArrayAccessExt) PolyLLVMExt.ext(c);
        }
        ext.guarded = true;
        return c;
    }

    @Override
    public Node desugar(DesugarLocally v) {
        if (!guarded)
            return desugarBoundsCheck(v, (ArrayAccess) node());
        return super.desugar(v);
    }

    protected Expr desugarBoundsCheck(DesugarLocally v, ArrayAccess n) {
        Position pos = n.position();

        ClassType exnType;
        try {
            exnType = (ClassType) v.ts.typeForName("java.lang.ArrayIndexOutOfBoundsException");
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }

        LocalDecl arrFlat = v.tnf.TempSSA("arr", n.array());
        Local arr = v.tnf.Local(pos, arrFlat);

        LocalDecl lenFlat = v.tnf.TempSSA("len", v.tnf.Field(pos, copy(arr), "length"));
        Local len = v.tnf.Local(pos, lenFlat);

        LocalDecl idxFlat = v.tnf.TempSSA("idx", n.index());
        Local idx = v.tnf.Local(pos, idxFlat);

        // Build bounds check.
        Expr zero = v.nf.IntLit(pos, IntLit.INT, 0).type(v.ts.Int());
        Expr tooSmall = v.nf.Binary(pos, copy(idx), Binary.LT, zero).type(v.ts.Boolean());
        Expr tooLarge = v.nf.Binary(pos, copy(idx), Binary.GE, copy(len)).type(v.ts.Boolean());
        Expr check = v.nf.Binary(pos, tooSmall, Binary.COND_OR, tooLarge).type(v.ts.Boolean());

        // Guard access with bounds check. Avoid duplicating side-effects.
        Throw throwExn = v.tnf.Throw(pos, exnType, Collections.singletonList(copy(idx)));
        Stmt guard = v.tnf.If(check, throwExn);
        Expr result = setGuarded(n.index(copy(idx)).array(copy(arr)));
        return v.tnf.ESeq(Arrays.asList(arrFlat, lenFlat, idxFlat, guard), result);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug location.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "load.arr.elem");
        v.addTranslation(n, load);
        return n;
    }

    /** Return a pointer to the appropriate element in the array. */
    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        if (!guarded)
            throw new InternalCompilerError("Unguarded array access should be desugared");

        ArrayAccess n = (ArrayAccess) node();

        lang().visitChildren(n, v);

        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef base = v.utils.buildJavaArrayBase(arr, n.type());
        LLVMValueRef offset = v.getTranslation(n.index());

        return v.utils.buildGEP(base, offset);
    }
}
