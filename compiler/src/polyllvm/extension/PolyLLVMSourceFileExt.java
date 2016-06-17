package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        SourceFile n = (SourceFile) node();
        LLVMSourceFile llf =
                v.nodeFactory().LLVMSourceFile(Position.compilerGenerated(),
                                               n.position().file(),
                                               n.source(),
                                               null,
                                               null,
                                               null);
        for (TopLevelDecl tld : n.decls()) {
            LLVMSourceFile sf = (LLVMSourceFile) v.getTranslation(tld);
            llf = llf.merge(sf);
        }
        return llf;
    }
}
