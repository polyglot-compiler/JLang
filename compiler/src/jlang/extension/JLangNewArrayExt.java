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
        } else if (n.dims().size() > 1) {
            LLVMValueRef[] len = n.dims().stream()
                    .map(v::<LLVMValueRef>getTranslation)
                    .toArray(LLVMValueRef[]::new);

            res = translateNewArray(v, len, n.type().toArray());
        } else {
            LLVMValueRef len = v.getTranslation(n.dims().get(0));
            res = translateNew1DArray(v, len, n.type().toArray());
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    public static LLVMValueRef translateNewArray(LLVMTranslator v, LLVMValueRef[] len, ArrayType type) {
        ClassType arrType = v.ts.ArrayObject();
        LLVMTypeRef createArrayType = v.utils.functionType(
                v.utils.toLL(arrType),
                v.utils.i8Ptr(),
                v.utils.ptrTypeRef(v.utils.i32()),
                v.utils.i32()
        );
        LLVMValueRef createArray = v.utils.getFunction(Constants.CREATE_ARRAY, createArrayType);
        LLVMValueRef name = v.utils.buildGlobalCStr(v.mangler.userVisibleEntityName(type));
        LLVMValueRef arrSize = LLVMConstInt(v.utils.i32(), len.length, 0);
        LLVMValueRef arrLen = v.utils.buildArrayAlloca("arr.len", v.utils.i32(), arrSize);

        for (int i = 0; i < len.length; i++) {
            LLVMValueRef gep = v.utils.buildGEP(arrLen, i);
            LLVMBuildStore(v.builder, len[i], gep);
        }

        return v.utils.buildFunCall(createArray, name, arrLen, arrSize);
    }

    public static LLVMValueRef translateNew1DArray(LLVMTranslator v, LLVMValueRef len, ArrayType type) {
        ClassType arrType = v.ts.ArrayObject();
        LLVMTypeRef create1DArrayType = v.utils.functionType(
                v.utils.toLL(arrType),
                v.utils.i8Ptr(),
                v.utils.i32()
        );
        LLVMValueRef create1DArray = v.utils.getFunction(Constants.CREATE_1D_ARRAY, create1DArrayType);
        LLVMValueRef name = v.utils.buildGlobalCStr(v.mangler.userVisibleEntityName(type));

        return v.utils.buildFunCall(create1DArray, name, len);
    }
}
