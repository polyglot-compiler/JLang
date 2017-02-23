package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();

        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(v.builder);

        LLVMBasicBlockRef firstBlock = LLVMGetFirstBasicBlock(v.currFn());
        LLVMPositionBuilderBefore(v.builder,LLVMGetBasicBlockTerminator(firstBlock));
        LLVMValueRef alloc = LLVMBuildAlloca(v.builder, LLVMUtils.typeRef(n.type().type(),v), n.name());
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
