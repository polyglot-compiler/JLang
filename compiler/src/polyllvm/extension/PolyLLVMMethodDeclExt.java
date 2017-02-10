package polyllvm.extension;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.MethodDecl;
import polyglot.util.SerialVersionUID;
import polyllvm.util.LLVMUtils;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMMethodDeclExt extends PolyLLVMProcedureDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    protected LLVMTypeRef llvmRetType(LLVM.LLVMModuleRef mod) {
        MethodDecl n = (MethodDecl) node();
        return LLVMUtils.typeRef(n.returnType().type(), mod);
    }
}
