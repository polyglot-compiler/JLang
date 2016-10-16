package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Node;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.*;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        //Get the pointer to the array element
        LLVMESeq elementPtr = translateArrayAccessPointer(n, v);

        //Load the array element

        LLVMVariable nonCastResult =
                PolyLLVMFreshGen.freshLocalVar(nf,
                                               nf.LLVMPointerType(nf.LLVMIntType(8)));

        LLVMLoad load =
                nf.LLVMLoad(nonCastResult,
                            nf.LLVMPointerType(nf.LLVMIntType(8)),
                            elementPtr);

        //Cast the array element

        LLVMTypeNode typeOfElement =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());

        LLVMVariable castResult =
                PolyLLVMFreshGen.freshLocalVar(nf, typeOfElement);

        Instruction conversionType = null;
        if (n.type().isLongOrLess()) {
            conversionType = LLVMConversion.PTRTOINT;
        }
        else if (n.type().isDouble()) {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }
        else if (n.type().isFloat()) {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }
        else if (n.type().isReference()) {
            conversionType = LLVMConversion.BITCAST;
        }
        else {
            throw new InternalCompilerError("Unhandled type for ArrayAccess: "
                    + n.type());
        }

        LLVMConversion cast = nf.LLVMConversion(conversionType,
                                                castResult,
                                                nf.LLVMPointerType(nf.LLVMIntType(8)),
                                                nonCastResult,
                                                typeOfElement);

        //Add the result to the translation
        LLVMInstruction seq = nf.LLVMSeq(CollectionUtil.list(load, cast));

        v.addTranslation(n, nf.LLVMESeq(seq, castResult));

        return super.translatePseudoLLVM(v);
    }

    public static LLVMESeq translateArrayAccessPointer(ArrayAccess n,
            PseudoLLVMTranslator v) {
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMOperand array = (LLVMOperand) v.getTranslation(n.array());
//      System.out.println("\nArrayAccess `" + n + "` translated to:  " + array
//              + "\n");

//        %_result.0 = getelementptr %class.classes.Array, %class.classes.Array* %_temp.6, i32 0, i32 2

        LLVMVariable result =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "result",
                                                    nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
        LLVMGetElementPtr gepArray =
                PolyLLVMFreshGen.freshGetElementPtr(nf,
                                                    result,
                                                    array,
                                                    0,
                                                    PolyLLVMConstants.ARRAYELEMENTOFFSET);

//      %_elementPtr.0 = getelementptr i8*, i8** %_result.0, i64 0

        LLVMVariable elementPtr =
                PolyLLVMFreshGen.freshNamedLocalVar(nf,
                                                    "elementPtr",
                                                    nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));

        LLVMOperand gepOperand = (LLVMOperand) v.getTranslation(n.index());
        LLVMVariable gepOperandResult =
                PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMIntType(64));
        LLVMConversion convertOperand =
                nf.LLVMConversion(LLVMConversion.SEXT,
                                  gepOperandResult,
                                  nf.LLVMIntType(32),
                                  gepOperand,
                                  nf.LLVMIntType(64));

        List<LLVMTypedOperand> gepOperands =
                CollectionUtil.list(nf.LLVMTypedOperand(nf.LLVMESeq(convertOperand,
                                                                    gepOperandResult),
                                                        nf.LLVMIntType(64)));

        LLVMGetElementPtr gepElement =
                nf.LLVMGetElementPtr(result, gepOperands).result(elementPtr);

        LLVMSeq instrsSeq =
                nf.LLVMSeq(CollectionUtil.list((LLVMInstruction) gepArray,
                                               gepElement));
        return nf.LLVMESeq(instrsSeq, elementPtr);

    }

}
