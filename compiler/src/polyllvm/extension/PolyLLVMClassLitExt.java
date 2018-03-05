package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.Node;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMClassLitExt extends PolyLLVMExt {
    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        // TODO: Unimplemented, but we don't want a compiler error for now.
        System.err.println("WARNING: ClassLit is unimplemented");
        v.addTranslation(node(), LLVM.LLVMConstNull(v.utils.toLL(v.typeSystem().Class())));
        return super.overrideTranslateLLVM(parent, v);
    }
}
