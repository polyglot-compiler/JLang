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
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

public class PolyLLVMFieldAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        FieldAssign n = (FieldAssign) node();
        Field field = n.left();
        Receiver objectTarget = field.target();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMNode expr = (LLVMNode) v.getTranslation(n.right());
        LLVMTypeNode fieldTypeNode = LLVMUtils.polyLLVMTypeNode(nf, field.type());

        if (!(expr instanceof LLVMOperand)) {
            throw new InternalCompilerError("Expression `" + n.right() + "` ("
                    + n.right().getClass()
                    + ") was not translated to an LLVMOperand " + "("
                    + v.getTranslation(n.right()) + ")");
        }

        if (field.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(field);
            LLVMTypeNode ptrTypeNode = nf.LLVMPointerType(fieldTypeNode);
            LLVMVariable.VarKind ptrKind = LLVMVariable.VarKind.GLOBAL;
            LLVMVariable ptr = nf.LLVMVariable(mangledGlobalName, ptrTypeNode, ptrKind);
            LLVMInstruction store = nf.LLVMStore(fieldTypeNode, (LLVMOperand) expr, ptr);
            v.addTranslation(n, store);

            LLVMGlobalVarDeclaration externDecl = nf.LLVMGlobalVarDeclaration(
                    mangledGlobalName,
                    /* isExtern */ true,
                    LLVMGlobalVarDeclaration.GLOBAL,
                    fieldTypeNode,
                    /* initValue */ null);
            v.addStaticVarReferenced(mangledGlobalName, externDecl);
        }
        else {
            // Instance fields.
            LLVMOperand objectTranslation =
                    (LLVMOperand) v.getTranslation(objectTarget);
            int fieldIndex = v.getFieldIndex((ReferenceType) objectTarget.type(),
                    field.fieldInstance());
            LLVMTypedOperand index0 =
                    nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32), 0),
                            nf.LLVMIntType(32));
            LLVMTypedOperand fieldIndexOperand =
                    nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32),
                            fieldIndex),
                            nf.LLVMIntType(32));
            List<LLVMTypedOperand> gepIndexList =
                    CollectionUtil.list(index0, fieldIndexOperand);
            LLVMVariable fieldPtr =
                    PolyLLVMFreshGen.freshLocalVar(nf,
                            nf.LLVMPointerType(fieldTypeNode));
            LLVMInstruction gep =
                    nf.LLVMGetElementPtr(objectTranslation, gepIndexList)
                            .result(fieldPtr);

            LLVMStore store = nf.LLVMStore(fieldTypeNode, (LLVMOperand) expr, fieldPtr);

            v.addTranslation(n, nf.LLVMSeq(CollectionUtil.list(gep, store)));
        }

        return super.translatePseudoLLVM(v);
    }

}
