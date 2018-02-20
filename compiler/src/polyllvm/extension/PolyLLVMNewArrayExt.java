package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
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
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.init() != null) {
            v.addTranslation(n, v.getTranslation(n.init()));
        }
        else {
            if (n.dims().size() > 1)
                throw new InternalCompilerError("Multidimensional arrays should be desugared");
            Expr len = n.dims().get(0);
            New newArray = translateNewArray(v, nf, len, n.type().toArray().base(), n.position());
            v.addTranslation(n, v.getTranslation(newArray));
        }

        return super.leaveTranslateLLVM(v);
    }

    public static New translateNewArray(LLVMTranslator v,
                                        PolyLLVMNodeFactory nf,
                                        Expr len,
                                        Type baseType,
                                        Position pos) {
        ClassType arrType = v.utils.getArrayType();
        TypeSystem ts = v.typeSystem();
        ConstructorInstance arrayConstructor;
        try {
            arrayConstructor = ts.findConstructor(
                    arrType, Collections.singletonList(ts.Int()), arrType, /*fromClient*/ true);
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
        int sizeOfType = v.utils.sizeOfType(baseType);

        CanonicalTypeNode newTypeNode = nf.CanonicalTypeNode(pos, arrType);
        New newArray = (New) nf.New(pos, newTypeNode, Collections.singletonList(len))
                           .constructorInstance(arrayConstructor)
                           .type(arrType);

        LLVMValueRef arrLen =  v.getTranslation(len);
        LLVMTypeRef i64 = LLVMInt64TypeInContext(v.context);
        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, arrLen, i64, "arr_len");
        LLVMValueRef elemSize = LLVMConstInt(i64, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef contentSize = LLVMBuildMul(v.builder, elemSize, arrLen64, "mul");
        LLVMValueRef headerSize = LLVMConstInt(i64, ARR_ELEM_OFFSET * v.utils.llvmPtrSize(), /*sign-extend*/ 0);
        LLVMValueRef size = LLVMBuildAdd(v.builder, headerSize, contentSize, "size");

        PolyLLVMNewExt ext = (PolyLLVMNewExt) PolyLLVMExt.ext(newArray);
        ext.translateWithSize(v, size);
        return newArray;
    }
}
