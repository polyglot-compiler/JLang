package polyllvm.extension;

import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.util.SerialVersionUID;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMMethodDeclExt extends PolyLLVMProcedureDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    protected LLVMTypeRef llvmRetType(LLVMModuleRef mod) {
        MethodDecl n = (MethodDecl) node();
        return  LLVMUtils.typeRef(n.returnType(), mod);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LLVMBuildRetVoid(v.builder);
        return super.translatePseudoLLVM(v);
    }
}
