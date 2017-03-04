package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMThrowExt extends PolyLLVMExt {

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Throw n = (Throw) node();
        LLVMValueRef allocateExnFunc = LLVMUtils.getFunction(v.mod, Constants.ALLOCATE_EXCEPTION,
                LLVMUtils.functionType(LLVMUtils.llvmBytePtr(), LLVMUtils.llvmBytePtr()));
        LLVMValueRef throwExnFunc = LLVMUtils.getFunction(v.mod, Constants.THROW_EXCEPTION,
                LLVMUtils.functionType(LLVMVoidType(), LLVMUtils.llvmBytePtr()));
        LLVMValueRef translation = LLVMBuildBitCast(v.builder, v.getTranslation(n.expr()), LLVMUtils.llvmBytePtr(), "cast");
        LLVMValueRef exn = LLVMUtils.buildMethodCall(v, allocateExnFunc, translation);
        LLVMUtils.buildProcedureCall(v, throwExnFunc, exn);
        LLVMBuildUnreachable(v.builder);
        return super.translatePseudoLLVM(v);
    }
}
