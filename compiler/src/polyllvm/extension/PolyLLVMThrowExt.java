package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMThrowExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Throw n = (Throw) node();
        LLVMValueRef createExnFun = v.utils.getFunction(Constants.CREATE_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMValueRef throwExnFunc = v.utils.getFunction(Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidTypeInContext(v.context), v.utils.llvmBytePtr()));
        LLVMValueRef cast = LLVMBuildBitCast(
                v.builder, v.getTranslation(n.expr()), v.utils.llvmBytePtr(), "cast");
        LLVMValueRef exn = v.utils.buildFunCall(createExnFun, cast);
        v.utils.buildProcCall(throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
        return super.leaveTranslateLLVM(v);
    }
}
