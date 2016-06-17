package polyllvm.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PrintVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

/**
 * Operations any PolyLLVM compatible node must implement.
 */
public interface PolyLLVMOps extends NodeOps {

    Node print(PrintVisitor v);

    Node removeStringLiterals(StringLiteralRemover v);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v);

    Node translatePseudoLLVM(PseudoLLVMTranslator v);

    Node addVoidReturn(AddVoidReturnVisitor v);

}
