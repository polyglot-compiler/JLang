package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

public class PolyLLVMFieldAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        FieldAssign n = (FieldAssign) node();
        Field target = n.left();
        Receiver objectTarget = target.target();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMNode expr = v.getTranslation(n.right());

        if (!(expr instanceof LLVMOperand)) {
            throw new InternalCompilerError("Expression `" + n.right() + "` ("
                    + n.right().getClass()
                    + ") was not translated to an LLVMOperand " + "("
                    + v.getTranslation(n.right()) + ")");
        }

        LLVMOperand objectTranslation =
                (LLVMOperand) v.getTranslation(objectTarget);
        int fieldIndex = v.getFieldIndex((ReferenceType) objectTarget.type(),
                                         target.fieldInstance());
        System.out.println("HERE: " + fieldIndex);
        System.out.println("" + node());
        LLVMTypedOperand index0 =
                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32), 0),
                                    nf.LLVMIntType(32));
        LLVMTypedOperand fieldIndexOperand =
                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                      fieldIndex),
                                    nf.LLVMIntType(32));
        List<LLVMTypedOperand> gepIndexList =
                CollectionUtil.list(index0, fieldIndexOperand);
        LLVMTypeNode fieldTypeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, target.type());
        LLVMVariable fieldPtr =
                PolyLLVMFreshGen.freshLocalVar(nf,
                                               nf.LLVMPointerType(fieldTypeNode));
        LLVMInstruction gep =
                nf.LLVMGetElementPtr(objectTranslation, gepIndexList)
                  .result(fieldPtr);

        LLVMStore store =
                nf.LLVMStore(fieldTypeNode, (LLVMOperand) expr, fieldPtr);

        v.addTranslation(n, nf.LLVMSeq(CollectionUtil.list(gep, store)));

        return super.translatePseudoLLVM(v);
    }

}
