package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSpecialExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Special n = (Special) node();

        if (n.qualifier() != null)
            throw new InternalCompilerError("Qualified this should have been desugared");

        LLVMValueRef thisPtr = LLVMGetParam(v.currFn(), 0);
        if (n.kind() == Special.THIS) {
            v.addTranslation(n, thisPtr);
        }
        else if (n.kind() == Special.SUPER) {
            LLVMTypeRef type = v.utils.toLL(n.type());
            LLVMValueRef to_super = LLVMBuildBitCast(v.builder, thisPtr, type, "cast_to_super");
            v.addTranslation(n, to_super);
        }

        return super.leaveTranslateLLVM(v);
    }
}
