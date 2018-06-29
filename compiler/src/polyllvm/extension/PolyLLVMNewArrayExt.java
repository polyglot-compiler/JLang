package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Collections;

import static org.bytedeco.javacpp.LLVM.*;
import static polyllvm.util.Constants.RUNTIME_ARRAY;
import static polyllvm.util.Constants.RUNTIME_ARRAY_TYPE;

public class PolyLLVMNewArrayExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        NewArray n = (NewArray) node();

        // Desugar multidimensional arrays.
        // (Note that array initializer expressions are an exception,
        // since they have their own translation.)
        if (n.init() == null && n.dims().size() > 1) {
            try {
                return desugarMultidimensional(v);
            } catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
        }

        return super.desugar(v);
    }

    /**
     * Desugars multidimensional arrays into a runtime call, which in turn builds the
     * multidimensional array by recursively building and initializing single dimensional arrays.
     */
    protected Expr desugarMultidimensional(DesugarLocally v) throws SemanticException {
        NewArray n = (NewArray) node();
        assert n.init() == null && n.dims().size() > 1;

        Position pos = n.position();
        ClassType arrType = v.ts.typeForName(RUNTIME_ARRAY).toClass();
        ReferenceType leafTypeEnum = v.ts.typeForName(RUNTIME_ARRAY_TYPE).toReference();

        Type leafType;
        if (n.additionalDims() > 0) {
            // If there are additional dims, then the leaf arrays store null array references.
            leafType = v.ts.Object();
        } else {
            leafType = n.type().toArray().ultimateBase();
        }
        String leafTypeStr = getLeafTypeString(leafType);
        Field leafTypeField = v.tnf.StaticField(pos, leafTypeEnum, leafTypeStr);
        ArrayInit lens = (ArrayInit) v.nf.ArrayInit(pos, n.dims()).type(v.ts.arrayOf(v.ts.Int()));
        String name = "createMultidimensional";
        return v.tnf.StaticCall(pos, arrType, arrType, name, leafTypeField, lens);
    }

    // Returns the name of the enum constant corresponding to the given array leaf type.
    // These constants exist in runtime code.
    private String getLeafTypeString(Type t) {
        if (t.isBoolean())   return "BOOLEAN";
        if (t.isByte())      return "BYTE";
        if (t.isChar())      return "CHAR";
        if (t.isShort())     return "SHORT";
        if (t.isInt())       return "INT";
        if (t.isLong())      return "LONG";
        if (t.isFloat())     return "FLOAT";
        if (t.isDouble())    return "DOUBLE";
        if (t.isReference()) return "OBJECT";
        throw new InternalCompilerError("Unhandled array leaf type");
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        NewArray n = (NewArray) node();

        LLVMValueRef res;
        if (n.init() != null) {
            // Steal the translation of the initializer.
            res = v.getTranslation(n.init());
        }
        else {
            if (n.dims().size() > 1)
                throw new InternalCompilerError("Multidimensional arrays should be desugared");
            LLVMValueRef len = v.getTranslation(n.dims().get(0));
            res = translateNewArray(v, len, n.type().toArray().base());
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    public static LLVMValueRef translateNewArray(LLVMTranslator v, LLVMValueRef len, Type elemT) {
        ClassType arrType = v.ts.ArrayObject();
        TypeSystem ts = v.ts;
        ConstructorInstance arrayConstructor;
        try {
            arrayConstructor = ts.findConstructor(
                    arrType, Collections.nCopies(2, ts.Int()), arrType, /*fromClient*/ true);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        int sizeOfType = v.utils.sizeOfType(elemT);

        LLVMTypeRef i64 = LLVMInt64TypeInContext(v.context);
        LLVMTypeRef i32 = LLVMInt32TypeInContext(v.context);
        LLVMValueRef elemSizeArg = LLVMConstInt(i32, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, len, i64, "arr.len");
        LLVMValueRef elemSize = LLVMConstInt(i64, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef contentSize = LLVMBuildMul(v.builder, elemSize, arrLen64, "mul");
        LLVMValueRef headerSize = v.obj.sizeOf(arrType);
        LLVMValueRef size = LLVMBuildAdd(v.builder, headerSize, contentSize, "size");

        LLVMValueRef[] arg = {len, elemSizeArg};
        return PolyLLVMNewExt.translateWithArgsAndSize(v, arg, size, arrayConstructor);
    }
}
