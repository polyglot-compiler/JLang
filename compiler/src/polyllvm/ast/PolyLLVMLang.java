package polyllvm.ast;

import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PrintVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public interface PolyLLVMLang extends JLang {
    // TODO: Declare any dispatch methods for new AST operations

    Node print(Node n, PrintVisitor v);

    Node removeStringLiterals(Node n, StringLiteralRemover llvmTranslation);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(Node n,
            PseudoLLVMTranslator v);

    Node translatePseudoLLVM(Node n, PseudoLLVMTranslator v);

    Node addVoidReturn(Node n, AddVoidReturnVisitor addVoidReturnVisitor);
}
