//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.Node;
import polyglot.ast.Throw;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.util.Constants;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangThrowExt extends JLangExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Throw n = (Throw) node();
        LLVMValueRef createExnFun = v.utils.getFunction(Constants.CREATE_EXCEPTION,
                v.utils.functionType(v.utils.i8Ptr(), v.utils.i8Ptr()));
        LLVMValueRef throwExnFunc = v.utils.getFunction(Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidTypeInContext(v.context), v.utils.i8Ptr()));
        LLVMValueRef cast = LLVMBuildBitCast(
                v.builder, v.getTranslation(n.expr()), v.utils.i8Ptr(), "cast");
        LLVMValueRef exn = v.utils.buildFunCall(createExnFun, cast);
        v.utils.buildProcCall(throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
        return super.leaveTranslateLLVM(v);
    }
}
