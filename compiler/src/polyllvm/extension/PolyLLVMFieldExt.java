package polyllvm.extension;

import polyglot.ast.Field;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.types.ReferenceType;
import polyglot.util.CollectionUtil;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

public class PolyLLVMFieldExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Field n = (Field) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Receiver target = n.target();
        if (n.flags().isStatic()) {
            System.out.println("I HAVENT DONE STATIC FIELD ACCESSES YET!!!");
        }
        else {
            LLVMOperand thisTranslation =
                    (LLVMOperand) v.getTranslation(target);
            int fieldIndex = v.getFieldIndex((ReferenceType) n.target().type(),
                                             n.fieldInstance());
            LLVMTypedOperand index0 = nf.LLVMTypedOperand(
                                                          nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                                            0),
                                                          nf.LLVMIntType(32));
            LLVMTypedOperand fieldIndexOperand =
                    nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                          fieldIndex),
                                        nf.LLVMIntType(32));

            List<LLVMTypedOperand> gepIndexList =
                    CollectionUtil.list(index0, fieldIndexOperand);
            LLVMTypeNode fieldTypeNode =
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
            LLVMVariable fieldPtr =
                    PolyLLVMFreshGen.freshLocalVar(nf,
                                                   nf.LLVMPointerType(fieldTypeNode));
            LLVMInstruction gep =
                    nf.LLVMGetElementPtr(thisTranslation, gepIndexList)
                      .result(fieldPtr);

            LLVMVariable field =
                    PolyLLVMFreshGen.freshLocalVar(nf, fieldTypeNode);
            LLVMLoad loadField = nf.LLVMLoad(field, fieldTypeNode, fieldPtr);
            v.addTranslation(n,
                             nf.LLVMESeq(nf.LLVMSeq(CollectionUtil.list(gep,
                                                                        loadField)),
                                         field));
        }
        return super.translatePseudoLLVM(v);
    }
}
