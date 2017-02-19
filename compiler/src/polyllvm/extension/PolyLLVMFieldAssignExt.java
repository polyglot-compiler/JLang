package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;
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
        LLVMValueRef expr = v.getTranslation(n.right());
        LLVMTypeRef fieldTypeRef = LLVMUtils.typeRef(field.type(), v);

        if (field.flags().isStatic()) {
            // Static fields.
            String mangledGlobalName = PolyLLVMMangler.mangleStaticFieldName(field);
            LLVMValueRef global = LLVMUtils.getGlobal(v.mod, mangledGlobalName, fieldTypeRef);
            LLVMValueRef store = LLVMBuildStore(v.builder, expr, global);
            v.addTranslation(n, store);
        }
        else {
            // Instance fields.
            LLVMValueRef objectTranslation = v.getTranslation(objectTarget);
            int fieldIndex = v.getFieldIndex((ReferenceType) objectTarget.type(),
                    field.fieldInstance());
            LLVMValueRef gep = LLVMUtils.buildGEP(v.builder,objectTranslation,
                    LLVMConstInt(LLVMInt32Type(), 0, /*sign extend*/ 0),
                    LLVMConstInt(LLVMInt32Type(), fieldIndex, /*sign extend*/ 0));
            v.addTranslation(n, LLVMBuildStore(v.builder, expr, gep));


        }

        return super.translatePseudoLLVM(v);
    }

}
