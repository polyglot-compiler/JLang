//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.ArrayInit;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangArrayInitExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();

        // Normally an empty ArrayInit could have ts.Null() as its base type,
        // but we fixed that in an earlier pass using the expected type of
        // this node with respect to its parent.
        if (!n.type().isArray())
            throw new InternalCompilerError("ArrayInit node does not have an array type");

        LLVMValueRef len = LLVMConstInt(
                v.utils.toLL(v.ts.Int()), n.elements().size(), /*signExtend*/ 0);
        LLVMValueRef array = JLangNewArrayExt.translateNewArray(v, len, n.type().toArray());

        if (!n.elements().isEmpty()) {
            LLVMValueRef base = v.obj.buildArrayBaseElementPtr(array, n.type().toArray());
            int idx = 0;
            for (Expr expr : n.elements()) {
                LLVMValueRef gep = v.utils.buildGEP(base, idx);
                LLVMBuildStore(v.builder, v.getTranslation(expr), gep);
                ++idx;
            }
        }

        v.addTranslation(n, array);
        return super.leaveTranslateLLVM(v);
    }
}
