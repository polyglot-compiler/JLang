//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangInstanceofExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Instanceof n = (Instanceof) node();
        LLVMValueRef obj =  v.getTranslation(n.expr());
        ReferenceType rt = n.compareType().type().toReference();
        LLVMValueRef compTypeIdVar = v.classObjs.toTypeIdentity(rt);
        LLVMTypeRef bytePtrTy = v.utils.ptrTypeRef(v.utils.intType(8));
        LLVMTypeRef objTy = v.utils.toLL(v.ts.Object());
        LLVMValueRef objBitcast = LLVMBuildBitCast(v.builder, obj, objTy, "cast.obj");
        LLVMTypeRef funcType = v.utils.functionType(v.utils.intType(1), objTy, bytePtrTy);
        LLVMValueRef function = v.utils.getFunction("InstanceOf", funcType);
        LLVMValueRef res = v.utils.buildFunCall(function, objBitcast, compTypeIdVar);
        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }
}
