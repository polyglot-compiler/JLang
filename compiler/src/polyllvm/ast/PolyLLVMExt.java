package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.visit.*;

public class PolyLLVMExt extends Ext_c implements PolyLLVMOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static PolyLLVMExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof PolyLLVMExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No PolyLLVM extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (PolyLLVMExt) e;
    }

    @Override
    public final PolyLLVMLang lang() {
        return PolyLLVMLang_c.instance;
    }

    @Override
    public Node print(PrintVisitor v) {
        System.out.println("NODE: " + node());
        return node();
    }

    @Override
    public Node removeStringLiterals(StringLiteralRemover v) {
        return node();
    }

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        return v;
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        return node();
    }

    @Override
    public Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        return null;
    }

    @Override
    public Node addVoidReturn(AddVoidReturnVisitor v) {
        return node();
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        return node();
    }

    @Override
    public Node llvmVarToStack(LLVMVarToStack v) {
        return node();
    }

    @Override
    public final Node removeESeq(RemoveESeqVisitor v) {
        return ((LLVMNode) node()).removeESeq(v);
    }

    @Override
    public AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(
            AddPrimitiveWideningCastsVisitor v) {
        return v;
    }

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        return node();
    }

}
