package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();

        LLVMValueRef alloc = v.utils.buildAlloca(n.name(), v.utils.toLL(n.declType()));
        v.addAllocation(n.name(), alloc);
        v.debugInfo.createLocalVariable(v, n, alloc);

        if (n.init() != null) {
            v.addTranslation(n, LLVMBuildStore(v.builder, v.getTranslation(n.init()), alloc));
        }

        return super.leaveTranslateLLVM(v);
    }
}
