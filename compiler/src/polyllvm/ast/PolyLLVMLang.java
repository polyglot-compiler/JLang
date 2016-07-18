package polyllvm.ast;

import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.LLVMVarToStack;
import polyllvm.visit.PrintVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.RemoveESeqVisitor;
import polyllvm.visit.StringLiteralRemover;

public interface PolyLLVMLang extends JLang {
    // TODO: Declare any dispatch methods for new AST operations

    Node print(Node n, PrintVisitor v);

    Node removeStringLiterals(Node n, StringLiteralRemover llvmTranslation);

    PseudoLLVMTranslator enterTranslatePseudoLLVM(Node n,
            PseudoLLVMTranslator v);

    Node translatePseudoLLVM(Node n, PseudoLLVMTranslator v);

    Node translatePseudoLLVMConditional(Node n, PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel);

    Node addVoidReturn(Node n, AddVoidReturnVisitor addVoidReturnVisitor);

    Node llvmVarToStack(Node n, LLVMVarToStack llvmVarToStack);

    Node removeESeq(Node n, RemoveESeqVisitor removeESeqVisitor);

    AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(Node n,
            AddPrimitiveWideningCastsVisitor v);

    Node addPrimitiveWideningCasts(Node n, AddPrimitiveWideningCastsVisitor v);
}
