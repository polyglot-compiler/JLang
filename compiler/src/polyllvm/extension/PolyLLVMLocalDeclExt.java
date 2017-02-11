package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();

        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(v.builder);

        LLVMBasicBlockRef firstBlock = LLVMGetFirstBasicBlock(v.currFn());
        LLVMPositionBuilderBefore(v.builder,LLVMGetBasicBlockTerminator(firstBlock));
        LLVMValueRef alloc = LLVMBuildAlloca(v.builder, LLVMUtils.typeRef(n.type().type(),v.mod), n.name());
        v.addAllocation(n.name(), alloc);

        LLVMPositionBuilderAtEnd(v.builder, currentBlock);

        if(n.init() == null){
            return super.translatePseudoLLVM(v);
        }

        LLVMValueRef init = v.getTranslation(n.init());
        v.addTranslation(n, LLVMBuildStore(v.builder, init, alloc));
        return super.translatePseudoLLVM(v);
    }
}
