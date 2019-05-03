//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.ast.JLangExt;
import jlang.util.Constants;
import jlang.visit.LLVMTranslator;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangNewArrayExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

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
