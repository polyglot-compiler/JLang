package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.*;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMArrayInitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<Expr> elements = n.elements();

        Expr intLit1 = nf
                         .IntLit(Position.compilerGenerated(),
                                 IntLit.INT,
                                 elements.size())
                         .type(v.typeSystem().Int());
        v.lang().translatePseudoLLVM(intLit1, v);
        List<Expr> dims = CollectionUtil.list(intLit1);

        New newArray = PolyLLVMNewArrayExt.translateArrayWithDims(v, nf, dims);
        LLVMValueRef array = v.getTranslation(newArray);

        //Create a Java local to construct ArrayAccessAssign to reuse translation
        //TODO: Make fresh name generation more robust
        Id arrayId = nf.Id(Position.compilerGenerated(),
                           PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMVoidType())
                                           .name().replace('.', '_'));
        Local arrayLocal = nf.Local(Position.compilerGenerated(), arrayId);
        v.addTranslation(arrayLocal, array);

        for (int i = 0; i < elements.size(); i++) {
            Expr expr = elements.get(i);
            IntLit intLitIndex = (IntLit) nf
                                            .IntLit(Position.compilerGenerated(),
                                                    IntLit.INT,
                                                    i)
                                            .type(v.typeSystem().Int());
            ArrayAccess arrayAccess =
                    (ArrayAccess) nf.ArrayAccess(Position.compilerGenerated(),
                                                 arrayLocal,
                                                 intLitIndex)
                                    .type(n.type().toArray().base());

            ArrayAccessAssign assign =
                    (ArrayAccessAssign) nf.ArrayAccessAssign(Position.compilerGenerated(),
                                                             arrayAccess,
                                                             Assign.ASSIGN,
                                                             expr)
                                          .type(n.type().toArray().base());

            v.lang().translatePseudoLLVM(intLitIndex, v);
            v.lang().translatePseudoLLVM(arrayAccess, v);
            v.lang().translatePseudoLLVM(assign, v);
        }

        v.addTranslation(n, array);
        return super.translatePseudoLLVM(v);
    }
}
