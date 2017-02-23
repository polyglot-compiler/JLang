package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewArrayExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        NewArray n = (NewArray) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.init() != null) {
            LLVMValueRef initializer = v.getTranslation(n.init());
            v.addTranslation(n, initializer);
        }
        else {
            List<Expr> dims = n.dims();
            New newArray = translateArrayWithDims(v, nf, dims, n.baseType().type());
            v.addTranslation(n, v.getTranslation(newArray));
        }
        return super.translatePseudoLLVM(v);
    }

    public static New translateArrayWithDims(PseudoLLVMTranslator v,
            PolyLLVMNodeFactory nf, List<Expr> dims, Type baseType) {
        New newArray;
        ReferenceType arrayType = v.getArrayType();
        List<Expr> args = new ArrayList<>();
        CanonicalTypeNode newTypeNode =
                nf.CanonicalTypeNode(Position.compilerGenerated(), arrayType);
        ConstructorInstance arrayConstructor;
        int sizeOfType;
        if (dims.size() == 1) {
            arrayConstructor =
                    getArrayConstructor(arrayType,
                                        ArrayConstructorType.ONEDIMENSIONAL);
            args.add(dims.get(0));
            sizeOfType = LLVMUtils.sizeOfType(baseType);
        }
        else {
            ArrayInit arrayDims =
                    (ArrayInit) nf.ArrayInit(Position.compilerGenerated(), dims)
                                  .type(v.typeSystem().Int().arrayOf());
            v.lang().translatePseudoLLVM(arrayDims, v);
            arrayConstructor =
                    getArrayConstructor(arrayType,
                                        ArrayConstructorType.MULTIDIMENSIONAL);
            args.add(arrayDims);
            sizeOfType = LLVMUtils.llvmPtrSize();

        }

        newArray =
                (New) nf.New(Position.compilerGenerated(), newTypeNode, args)
                        .constructorInstance(arrayConstructor)
                        .type(arrayType);

        v.lang().translatePseudoLLVM(newTypeNode, v);

        //Translate the newArray - need to set up a variable for the size
        // Size = 16 + length*sizeOfType
        LLVMValueRef arrayLength =  v.getTranslation(dims.get(0));


        // TODO: Allocate the right size for packed arrays.

        LLVMValueRef arrLen64 = LLVMBuildSExt(v.builder, arrayLength, LLVMInt64Type(), "arrLen");
        LLVMValueRef mul = LLVMBuildMul(v.builder,
                LLVMConstInt(LLVMInt64Type(), sizeOfType, /*sign-extend*/ 0),
                arrLen64, "mul");
        LLVMValueRef size = LLVMBuildAdd(v.builder,
                LLVMConstInt(LLVMInt64Type(), LLVMUtils.llvmPtrSize()*2 /*2 header words*/, /*sign-extend*/0),
                mul, "size");

        PolyLLVMNewExt extension = (PolyLLVMNewExt) PolyLLVMExt.ext(newArray);
        extension.translateWithSize(v, size);
        return newArray;
    }

    private enum ArrayConstructorType {
        ONEDIMENSIONAL, MULTIDIMENSIONAL
    }

    private static ConstructorInstance getArrayConstructor(
            ReferenceType arrayType, ArrayConstructorType arrType) {
        ConstructorInstance arrayConstructor = null;
        for (MemberInstance member : arrayType.members()) {
            if (member instanceof ConstructorInstance) {
                ConstructorInstance constructor = (ConstructorInstance) member;
                Type formalType = constructor.formalTypes().get(0); // Both constructors have only one formal type

                if (formalType instanceof ArrayType
                        && arrType == ArrayConstructorType.MULTIDIMENSIONAL) {
                    arrayConstructor = constructor;
                }
                else if (formalType instanceof PrimitiveType
                        && arrType == ArrayConstructorType.ONEDIMENSIONAL) {
                    arrayConstructor = constructor;
                }

            }
        }
        return arrayConstructor;
    }

}
