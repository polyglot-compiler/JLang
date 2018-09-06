//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.types.FieldInstance;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangFieldExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Field n = (Field) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug info.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "load." + n.name());
        v.addTranslation(n, load);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        Field n = (Field) node();
        FieldInstance fi = n.fieldInstance();
        n.visitChild(n.target(), v);

        if (n.flags().isStatic()) {
            // Ensure containing class is initialized. See JLS 7, section 12.4.1.
            // TODO: Can optimize this to avoid checks when inside the container class.
            v.utils.buildClassLoadCheck(fi.container().toClass());
            return v.utils.getStaticField(fi);
        }
        else {
            LLVMValueRef instance = v.getTranslation(n.target());
            LLVMValueRef ptr = v.obj.buildFieldElementPtr(instance, fi);

            // Bitcast needed due to potential mismatch introduced by erasure.
            return v.utils.toBitcastL(ptr, n.type());
        }
    }
}
