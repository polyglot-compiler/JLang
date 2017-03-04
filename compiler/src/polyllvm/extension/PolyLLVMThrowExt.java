package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMThrowExt extends PolyLLVMExt {

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        Throw n = (Throw) node();
        LLVMValueRef allocateExnFunc = v.utils.getFunction(v.mod, Constants.ALLOCATE_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMValueRef throwExnFunc = v.utils.getFunction(v.mod, Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidType(), v.utils.llvmBytePtr()));
        LLVMValueRef translation = LLVMBuildBitCast(v.builder, v.getTranslation(n.expr()), v.utils.llvmBytePtr(), "cast");
        LLVMValueRef exn = v.utils.buildMethodCall(allocateExnFunc, translation);
        v.utils.buildProcedureCall(throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
        return super.translatePseudoLLVM(v);
    }
}
