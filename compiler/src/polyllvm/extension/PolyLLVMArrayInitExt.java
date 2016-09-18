package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.ArrayInit;
import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.IntLit;
import polyglot.ast.Local;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.Stmt;
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

public class PolyLLVMArrayInitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<Expr> elements = n.elements();

        List<LLVMInstruction> instrs = new ArrayList<>();

        Expr intLit1 = nf
                         .IntLit(Position.compilerGenerated(),
                                 IntLit.INT,
                                 elements.size())
                         .type(v.typeSystem().Int());
        v.lang().translatePseudoLLVM(intLit1, v);
        List<Expr> dims = CollectionUtil.list(intLit1);

        New newArray = PolyLLVMNewArrayExt.translateArrayWithDims(v, nf, dims);
        LLVMESeq createdArray = (LLVMESeq) v.getTranslation(newArray); //Will always be eseq as new objects are eseq
        instrs.add(createdArray.instruction()); //Start with the empty array creation code

        LLVMOperand arrayExpr = createdArray.expr();

        //Create a Java local to construct ArrayAccessAssign to reuse translation
        Id arrayId = nf.Id(Position.compilerGenerated(),
                           PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMVoidType())
                                           .name());
        Local arrayLocal = nf.Local(Position.compilerGenerated(), arrayId);
        v.addTranslation(arrayLocal, arrayExpr);

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
            Stmt eval = nf.Eval(Position.compilerGenerated(), assign);

            v.lang().translatePseudoLLVM(intLitIndex, v);
            v.lang().translatePseudoLLVM(arrayAccess, v);
            v.lang().translatePseudoLLVM(assign, v);
            v.lang().translatePseudoLLVM(eval, v);

//            System.out.println("Assignment for element #" + i + " is :" + eval
//                    + " --> " + v.getTranslation(eval));

            instrs.add((LLVMInstruction) v.getTranslation(eval));

        }

        v.addTranslation(n, nf.LLVMESeq(nf.LLVMSeq(instrs), arrayExpr));
        return super.translatePseudoLLVM(v);
    }
}
