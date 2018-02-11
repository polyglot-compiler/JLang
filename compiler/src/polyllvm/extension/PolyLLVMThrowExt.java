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
        buildThrow(v, v.getTranslation(n.expr()));
        return super.leaveTranslateLLVM(v);
    }

    /** Throw the given Java throwable instance  */
    public static void buildThrow(LLVMTranslator v, LLVMValueRef throwable) {
        LLVMValueRef createExnFun = v.utils.getFunction(v.mod, Constants.CREATE_EXCEPTION,
                v.utils.functionType(v.utils.llvmBytePtr(), v.utils.llvmBytePtr()));
        LLVMValueRef throwExnFunc = v.utils.getFunction(v.mod, Constants.THROW_EXCEPTION,
                v.utils.functionType(LLVMVoidTypeInContext(v.context), v.utils.llvmBytePtr()));
        LLVMValueRef cast = LLVMBuildBitCast(v.builder, throwable, v.utils.llvmBytePtr(), "cast");
        LLVMValueRef exn = v.utils.buildFunCall(createExnFun, cast);
        v.utils.buildProcCall(throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
    }
}
