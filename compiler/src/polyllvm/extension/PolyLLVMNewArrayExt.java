package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
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
            New newArray = translateArrayWithDims(v, nf, dims);
            v.addTranslation(n, v.getTranslation(newArray));
        }
        return super.translatePseudoLLVM(v);
    }

    public static New translateArrayWithDims(PseudoLLVMTranslator v,
            PolyLLVMNodeFactory nf, List<Expr> dims) {
        New newArray;
        ReferenceType arrayType = v.getArrayType();
        List<Expr> args = new ArrayList<>();
        CanonicalTypeNode newTypeNode =
                nf.CanonicalTypeNode(Position.compilerGenerated(), arrayType);
        ConstructorInstance arrayConstructor;

        if (dims.size() == 1) {
            arrayConstructor =
                    getArrayConstructor(arrayType,
                                        ArrayConstructorType.ONEDIMENSIONAL);
            args.add(dims.get(0));
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
        }

        newArray =
                (New) nf.New(Position.compilerGenerated(), newTypeNode, args)
                        .constructorInstance(arrayConstructor)
                        .type(arrayType);

        v.lang().translatePseudoLLVM(newTypeNode, v);

        //Translate the newArray - need to set up a variable for the size
        //Size = (2 + length) * 8
        LLVMValueRef arrayLength =  v.getTranslation(dims.get(0));
        LLVMValueRef addTwo = LLVMBuildAdd(v.builder, LLVMConstInt(LLVMInt32Type(), 2, /*sign-extend*/ 0), arrayLength, "addTwo");
        LLVMValueRef size = LLVMBuildMul(v.builder, addTwo, LLVMConstInt(LLVMInt32Type(), 8, /*sign-extend*/ 0), "size");
        LLVMValueRef size64 = LLVMBuildSExt(v.builder, size, LLVMInt64Type(), "size64");

        PolyLLVMNewExt extension = (PolyLLVMNewExt) PolyLLVMExt.ext(newArray);

        // TODO: Allocate the right size for packed arrays.
        extension.translateWithSize(v, size64);
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
