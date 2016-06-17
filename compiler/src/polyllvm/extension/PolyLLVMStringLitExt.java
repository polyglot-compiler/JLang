package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.StringLit;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.PolyLLVMStringUtils;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public class PolyLLVMStringLitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node removeStringLiterals(StringLiteralRemover v) {
        StringLit n = (StringLit) node();
        NodeFactory nf = v.nodeFactory();
        TypeSystem ts = v.typeSystem();
        return PolyLLVMStringUtils.stringToConstructor(n, nf, ts);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        throw new InternalCompilerError("String literals must be removed before"
                + " translation to pseudollvm");
    }
}
