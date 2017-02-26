package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSpecialExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Special n = (Special) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.qualifier() != null) {
            throw new InternalCompilerError("Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        v.debugInfo.emitLocation(n);

        if (n.kind() == Special.THIS) {
            v.addTranslation(n, LLVMGetParam(v.currFn(), 0));
        }
        else if (n.kind() == Special.SUPER) {
            LLVMValueRef to_super = LLVMBuildBitCast(v.builder,
                    LLVMGetParam(v.currFn(), 0), LLVMUtils.typeRef(n.type(), v), "cast_to_super");
            v.addTranslation(n, to_super);
        }

        return super.translatePseudoLLVM(v);
    }

}
