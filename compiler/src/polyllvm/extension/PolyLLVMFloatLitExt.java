package polyllvm.extension;

import polyglot.ast.FloatLit;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMFloatLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        FloatLit n = (FloatLit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (n.kind() == FloatLit.FLOAT) {
            v.addTranslation(n,
                             nf.LLVMFloatLiteral(nf.LLVMFloatType(),
                                                 (float) n.value()));
        }
        else if (n.kind() == FloatLit.DOUBLE) {
            v.addTranslation(n,
                             nf.LLVMDoubleLiteral(nf.LLVMDoubleType(),
                                                  n.value()));
        }
        else {
            throw new InternalCompilerError("Unhandled FloatLit kind: "
                    + n.kind());
        }
        return super.translatePseudoLLVM(v);
    }

}
