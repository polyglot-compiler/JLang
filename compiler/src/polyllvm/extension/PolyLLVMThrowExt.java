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
        LLVMValueRef allocateExnFunc = v.utils.getFunction(v.mod, Constants.ALLOCATE_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMValueRef throwExnFunc = v.utils.getFunction(v.mod, Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidTypeInContext(v.context), v.utils.llvmBytePtr()));
        LLVMValueRef translation = LLVMBuildBitCast(v.builder, v.getTranslation(n.expr()), v.utils.llvmBytePtr(), "cast");
        LLVMValueRef exn = v.utils.buildFunCall(allocateExnFunc, translation);
        v.utils.buildProcCall(throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
        return super.leaveTranslateLLVM(v);
    }
}
