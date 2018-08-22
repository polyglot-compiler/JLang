package jlang.extension;

import polyglot.ast.Node;

import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.ast.AddressOf;
import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

public class JLangAddressOfExt extends JLangExt {

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        AddressOf n = (AddressOf) node();
        LLVMValueRef ptr = lang().translateAsLValue(n.expr(), v);
        v.addTranslation(n, ptr);
        return super.leaveTranslateLLVM(v);
    }
}
