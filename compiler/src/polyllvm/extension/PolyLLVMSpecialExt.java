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
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Special n = (Special) node();

        if (n.qualifier() != null) {
            throw new InternalCompilerError("Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        v.debugInfo.emitLocation(n);

        if (n.kind() == Special.THIS) {
            v.addTranslation(n, LLVMGetParam(v.currFn(), 0));
        }
        else if (n.kind() == Special.SUPER) {
            LLVMValueRef to_super = LLVMBuildBitCast(v.builder,
                    LLVMGetParam(v.currFn(), 0), v.utils.typeRef(n.type()), "cast_to_super");
            v.addTranslation(n, to_super);
        }

        return super.translatePseudoLLVM(v);
    }

}
