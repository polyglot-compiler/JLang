//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.util.Constants;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

import java.lang.Override;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import jlang.ast.JLangExt;
import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

import static jlang.util.Constants.RUNTIME_ARRAY;
import static jlang.util.Constants.RUNTIME_ARRAY_TYPE;
import static org.bytedeco.javacpp.LLVM.*;

public class JLangNewArrayExt extends JLangExt {
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
            res = translateNewArray(v, len, n.type().toArray());
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    public static LLVMValueRef translateNewArray(LLVMTranslator v, LLVMValueRef len, ArrayType type) {
        ClassType arrType = v.ts.ArrayObject();
        TypeSystem ts = v.ts;
        ConstructorInstance arrayConstructor;
        try {
            arrayConstructor = ts.findConstructor(
                    arrType, Collections.nCopies(2, ts.Int()), arrType, /*fromClient*/ true);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        int sizeOfType = v.utils.sizeOfType(type.base());

        LLVMTypeRef i64 = LLVMInt64TypeInContext(v.context);
        LLVMTypeRef i32 = LLVMInt32TypeInContext(v.context);
        LLVMValueRef elemSizeArg = LLVMConstInt(i32, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, len, i64, "arr.len");
        LLVMValueRef elemSize = LLVMConstInt(i64, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef contentSize = LLVMBuildMul(v.builder, elemSize, arrLen64, "mul");
        LLVMValueRef headerSize = v.obj.sizeOf(arrType);
        LLVMValueRef size = LLVMBuildAdd(v.builder, headerSize, contentSize, "size");

        LLVMValueRef[] arg = {len, elemSizeArg};

        // LLVMTranslator v, LLVMValueRef[] args, LLVMValueRef size, ConstructorInstance ci

        ReferenceType clazz = arrayConstructor.container();

        // Allocate space for the new object.
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef obj = v.utils.buildFunCall(calloc, size);

        // Bitcast object
        LLVMValueRef objCast = LLVMBuildBitCast(v.builder, obj, v.utils.toLL(clazz), "obj_cast");

        // Set the Dispatch vector
        // TODO: Could probably do this inside the constructor function instead?
        LLVMValueRef gep = v.obj.buildDispatchVectorElementPtr(objCast, clazz);

        LLVMValueRef initDV = v.utils.getFunction(Constants.INIT_ARRAY_DV_FUNC,
                v.utils.functionType(v.utils.ptrTypeRef(v.dv.structTypeRef(clazz)), v.utils.i8Ptr()));
        // TODO: should use className instead of signature name
        LLVMValueRef name = v.utils.buildGlobalCStr(v.mangler.jniUnescapedSignature(type));
        LLVMValueRef dv = v.utils.buildFunCall(initDV, name);

        LLVMBuildStore(v.builder, dv, gep);

        // Call the constructor function
        String mangledFuncName = v.mangler.proc(arrayConstructor);

        LLVMTypeRef funcType = v.utils.toLL(arrayConstructor);
        LLVMValueRef funcPtr = v.utils.getFunction(mangledFuncName, funcType);

        // Bitcast the function so that the formal types are the types that
        // the arguments were cast to by DesugarImplicitConversions. It is
        // needed due to potential mismatch between the types caused by erasure.
        LLVMTypeRef funcTyCast = v.utils.toLLFuncTy(
                clazz, v.ts.Void(), arrayConstructor.formalTypes());
        funcPtr = LLVMBuildBitCast(v.builder, funcPtr, v.utils.ptrTypeRef(funcTyCast), "cast");

        LLVMValueRef[] llvmArgs = Stream.concat(
                Stream.of(objCast), Arrays.stream(arg))
                .toArray(LLVMValueRef[]::new);
        v.utils.buildProcCall(funcPtr, llvmArgs);

        return objCast;
    }
}
