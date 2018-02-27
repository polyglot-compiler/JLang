package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.types.LocalInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        LocalInstance li = n.localInstance().orig();
        LLVMTypeRef typeRef = v.utils.toLL(n.declType());

        LLVMValueRef alloca = v.utils.buildAlloca(n.name(), typeRef);
        v.addTranslation(li, alloca);
        v.debugInfo.createLocalVariable(v, n, alloca);

        if (n.init() != null) {
            LLVMBuildStore(v.builder, v.getTranslation(n.init()), alloca);
        }

        return super.leaveTranslateLLVM(v);
    }
}
