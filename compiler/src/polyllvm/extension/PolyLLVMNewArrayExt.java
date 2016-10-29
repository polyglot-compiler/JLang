package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMMul;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMNewArrayExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        NewArray n = (NewArray) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.init() != null) {
            LLVMOperand initializer = (LLVMOperand) v.getTranslation(n.init());
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

            newArray = (New) nf
                               .New(Position.compilerGenerated(),
                                    newTypeNode,
                                    args)
                               .constructorInstance(arrayConstructor)
                               .type(arrayType);

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
        LLVMOperand arrayLength = (LLVMOperand) v.getTranslation(dims.get(0));

        if (arrayLength instanceof LLVMESeq) {
            LLVMVariable replacementVar =
                    PolyLLVMFreshGen.freshLocalVar(nf, arrayLength.typeNode());
            LLVMESeq eseqLength = (LLVMESeq) arrayLength;
            LLVMInstruction instruction =
                    eseqLength.instruction().result(replacementVar);

            arrayLength =
                    eseqLength.instruction(instruction).expr(replacementVar);
        }

        LLVMVariable addTwoVar =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                        "addTwoVar",
                        nf.LLVMIntType(32));
        LLVMAdd addTwo = nf.LLVMAdd(nf.LLVMIntType(32),
                nf.LLVMIntLiteral(nf.LLVMIntType(32), 2),
                arrayLength)
                .result(addTwoVar);


        LLVMVariable mulByEightVar =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "mulByEightVar",
                                                    nf.LLVMIntType(32));
        LLVMMul mulByEight = nf
                               .LLVMMul(nf.LLVMIntType(32),
                                        nf.LLVMIntLiteral(nf.LLVMIntType(32),
                                                          8),
                                        addTwoVar)
                               .result(mulByEightVar);
        System.out.println("dims 0th element is: " + dims.get(0)
                + "\nArray Length variable is: " + arrayLength);



        LLVMVariable sizeCastVar =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "sizeCastVar",
                                                    nf.LLVMIntType(64));
        LLVMConversion sizeCast =
                nf.LLVMConversion(LLVMConversion.SEXT,
                                  sizeCastVar,
                                  nf.LLVMIntType(32),
                                  mulByEightVar,
                                  nf.LLVMIntType(64));

        LLVMESeq size = nf.LLVMESeq(
                                    nf.LLVMSeq(CollectionUtil.list(addTwo,
                                            mulByEight, sizeCast)),
                                    sizeCastVar);

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
