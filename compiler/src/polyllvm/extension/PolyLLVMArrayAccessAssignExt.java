package polyllvm.extension;

import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Node;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMArrayAccessAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ArrayAccessAssign n = (ArrayAccessAssign) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMOperand element = (LLVMOperand) v.getTranslation(n.right());

        LLVMESeq elementPtr =
                PolyLLVMArrayAccessExt.translateArrayAccessPointer(n.left(), v);

        LLVMTypeNode typeOfExpr =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.right().type());

        LLVMVariable castResult =
                PolyLLVMFreshGen.freshLocalVar(nf,
                                               nf.LLVMPointerType(nf.LLVMIntType(8)));
        LLVMInstruction cast;
        if (n.type().isLongOrLess()) {
            cast = nf.LLVMConversion(LLVMConversion.INTTOPTR,
                                     castResult,
                                     typeOfExpr,
                                     element,
                                     nf.LLVMPointerType(nf.LLVMIntType(8)));
        }
        else if (n.type().isDouble()) {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }
        else if (n.type().isFloat()) {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }
        else if (n.type().isReference()) {
            cast = nf.LLVMConversion(LLVMConversion.BITCAST,
                                     castResult,
                                     typeOfExpr,
                                     element,
                                     nf.LLVMPointerType(nf.LLVMIntType(8)));
        }
        else {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }

        LLVMStore store = nf.LLVMStore(nf.LLVMPointerType(nf.LLVMIntType(8)),
                                       castResult,
                                       elementPtr);

        v.addTranslation(n,
                         nf.LLVMESeq(nf.LLVMSeq(CollectionUtil.list(cast,
                                                                    store)),
                                     element));

        return super.translatePseudoLLVM(v);
    }

}
