package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.types.PolyLLVMLocalInstance;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        PolyLLVMLocalInstance li = (PolyLLVMLocalInstance) n.localInstance().orig();
        LLVMTypeRef typeRef = v.utils.toLL(n.declType());

        LLVMValueRef translation;
        if (li.isSSA()) {
            // If SSA, just forward the initializer value.
            if (n.init() == null)
                throw new InternalCompilerError(
                        "Variable marked SSA, but the declaration has no initial value");
            translation = v.getTranslation(n.init());
        }
        else {
            // Otherwise, allocate on the stack.
            translation = v.utils.buildAlloca(n.name(), typeRef);
            if (n.init() != null) {
                LLVMBuildStore(v.builder, v.getTranslation(n.init()), translation);
            }
        }

        // Declare debug information if this variable is visible to the user.
        if (!li.isTemp()) {
            v.debugInfo.createLocalVariable(v, n, translation);
        }

        v.addTranslation(li, translation);
        return super.leaveTranslateLLVM(v);
    }
}
