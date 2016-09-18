package polyllvm.extension;

import polyglot.ast.Empty;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMEmptyExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Empty n = (Empty) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        v.addTranslation(n,
                         nf.LLVMAdd(PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                                        "NOP",
                                                                        nf.LLVMIntType(64)),
                                    nf.LLVMIntType(64),
                                    nf.LLVMIntLiteral(nf.LLVMIntType(64), 0),
                                    nf.LLVMIntLiteral(nf.LLVMIntType(64), 0)));
        return super.translatePseudoLLVM(v);
    }

}
