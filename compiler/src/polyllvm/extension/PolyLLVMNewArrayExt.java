package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

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
            List<Expr> dims = n.dims();
            v.debugInfo.emitLocation(n);
            New newArray = translateNewArray(v, nf, dims, n.baseType().type(), n.position());
            v.addTranslation(n, v.getTranslation(newArray));
        }

        return super.leaveTranslateLLVM(v);
    }

    public static New translateNewArray(LLVMTranslator v,
                                        PolyLLVMNodeFactory nf,
                                        List<Expr> dims,
                                        Type baseType,
                                        Position pos) {
        New newArray;
        ReferenceType arrType = v.utils.getArrayType();
        List<Expr> args = new ArrayList<>();
        ConstructorInstance arrayConstructor;

        int sizeOfType; // in byte
        if (dims.size() == 1) {
            arrayConstructor = getArrayConstructor(arrType, /*multidimensional*/ false);
            args.add(dims.iterator().next());
            sizeOfType = v.utils.sizeOfType(baseType);
        }
        else {
            ArrayInit arrayDims = (ArrayInit) nf.ArrayInit(pos, dims)
                                                .type(v.typeSystem().Int().arrayOf());
            arrayConstructor = getArrayConstructor(arrType, /*multidimensional*/ true);
            args.add(arrayDims);
            sizeOfType = LLVMUtils.llvmPtrSize();
        }

        CanonicalTypeNode newTypeNode = nf.CanonicalTypeNode(pos, arrType);
        newArray = (New) nf.New(pos, newTypeNode, args)
                           .constructorInstance(arrayConstructor)
                           .type(arrType);

        LLVMValueRef arrLen =  v.getTranslation(dims.iterator().next());
        LLVMTypeRef i64 = LLVMInt64TypeInContext(v.context);
        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, arrLen, i64, "arr_len");
        LLVMValueRef elemSize = LLVMConstInt(i64, sizeOfType, /*sign-extend*/ 0);
        LLVMValueRef contentSize = LLVMBuildMul(v.builder, elemSize, arrLen64, "mul");
        LLVMValueRef headerSize = LLVMConstInt(i64, Constants.ARR_ELEM_OFFSET*LLVMUtils.llvmPtrSize(), /*sign-extend*/ 0);
        LLVMValueRef size = LLVMBuildAdd(v.builder, headerSize, contentSize, "size");

        PolyLLVMNewExt ext = (PolyLLVMNewExt) PolyLLVMExt.ext(newArray);
        ext.translateWithSize(v, size);
        return newArray;
    }

    private static ConstructorInstance getArrayConstructor(ReferenceType arrayType,
                                                           boolean multidimensional) {
        // See the support.Array class implemented in the runtime.
        // Both array constructors have only one formal type; int for single-dimensional arrays,
        // and int[] for multidimensional arrays.
        for (MemberInstance member : arrayType.members()) {
            if (member instanceof ConstructorInstance) {
                ConstructorInstance constructor = (ConstructorInstance) member;
                Type formalType = constructor.formalTypes().iterator().next();
                if (formalType instanceof ArrayType && multidimensional) {
                    return constructor;
                } else if (formalType instanceof PrimitiveType && !multidimensional) {
                    return constructor;
                }
            }
        }

        throw new InternalCompilerError("Internal array constructor not found");
    }
}
