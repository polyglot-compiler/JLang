package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable.VarKind;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalAssignExt extends PolyLLVMAssignExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalAssign n = (LocalAssign) node();
        Local target = n.left();

        LLVMValueRef expr = v.getTranslation(n.right());
        expr = LLVMBuildBitCast(v.builder, expr, LLVMUtils.typeRef(n.type(), v), "assign_cast");
        v.addTranslation(n,LLVMBuildStore(v.builder, expr, v.getVariable(target.name())));

        return super.translatePseudoLLVM(v);
    }

}
