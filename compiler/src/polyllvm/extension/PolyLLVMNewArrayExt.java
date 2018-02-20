package polyllvm.extension;

import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Collections;

import static org.bytedeco.javacpp.LLVM.*;
import static polyllvm.util.Constants.ARR_ELEM_OFFSET;

public class PolyLLVMNewArrayExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        NewArray n = (NewArray) node();

        LLVMValueRef res;
        if (n.init() != null) {
            // Steal the translation of the initializer.
            res = v.getTranslation(n.init());
        }
        else {
            if (n.dims().size() > 1)
                throw new InternalCompilerError("Multidimensional arrays should be desugared");
            LLVMValueRef len = v.getTranslation(n.dims().get(0));
            res = translateNewArray(v, len, n.type().toArray().base());
        }

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }

    public static LLVMValueRef translateNewArray(LLVMTranslator v, LLVMValueRef len, Type elemT) {
        ClassType arrType = v.typeSystem().Array();
        TypeSystem ts = v.typeSystem();
        ConstructorInstance arrayConstructor;
        try {
            arrayConstructor = ts.findConstructor(
                    arrType, Collections.singletonList(ts.Int()), arrType, /*fromClient*/ true);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        int sizeOfType = v.utils.sizeOfType(elemT);

        LLVMTypeRef i64 = LLVMInt64TypeInContext(v.context);
        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, len, i64, "arr.len");
        LLVMValueRef elemSize = LLVMConstInt(i64, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef contentSize = LLVMBuildMul(v.builder, elemSize, arrLen64, "mul");
        LLVMValueRef headerSize = LLVMConstInt(i64, ARR_ELEM_OFFSET * v.utils.llvmPtrSize(), /*sign-extend*/ 0);
        LLVMValueRef size = LLVMBuildAdd(v.builder, headerSize, contentSize, "size");

        LLVMValueRef[] arg = {len};
        return PolyLLVMNewExt.translateWithArgsAndSize(v, arg, size, arrayConstructor);
    }
}
