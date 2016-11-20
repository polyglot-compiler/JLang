package polyllvm.extension;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.Arrays;
import java.util.List;

public class PolyLLVMInstanceofExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Instanceof n = (Instanceof) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMOperand obj = (LLVMOperand) v.getTranslation(n.expr());
        ReferenceType compareRt = n.compareType().type().toReference();
        LLVMOperand compTypeIdVar = ClassObjects.classIdVar(nf, compareRt);

        // Declared the class id variable for the compare type.
        LLVMGlobalVarDeclaration compTypeIdDecl =
                ClassObjects.classIdDecl(nf, compareRt, /* extern */ true);
        v.addStaticVarReferenced(compTypeIdDecl.name(), compTypeIdDecl);

        // Cast obj to a byte pointer.
        LLVMTypeNode bytePtrType = nf.LLVMPointerType(nf.LLVMIntType(8));
        LLVMVariable objBytePtr = PolyLLVMFreshGen.freshLocalVar(nf, bytePtrType);
        LLVMConversion objBitcast = nf.LLVMConversion(
                LLVMConversion.BITCAST,
                objBytePtr, obj.typeNode(),
                obj, bytePtrType
        );

        // Build call to native code.
        List<LLVMTypeNode> argTypes = Arrays.asList(bytePtrType, bytePtrType);
        LLVMTypeNode retType = nf.LLVMIntType(1);
        LLVMFunctionType funcType = nf.LLVMFunctionType(argTypes, retType);
        LLVMVariable funcVar = nf.LLVMVariable("instanceof", funcType, LLVMVariable.VarKind.GLOBAL);
        List<Pair<LLVMTypeNode, LLVMOperand>> args = Arrays.asList(
                new Pair<>(bytePtrType, objBytePtr),
                new Pair<>(bytePtrType, compTypeIdVar)
        );
        LLVMVariable ret = PolyLLVMFreshGen.freshLocalVar(nf, retType);
        LLVMCall call = nf.LLVMCall(funcVar, args, retType).result(ret);
        v.addStaticCall(call);

        LLVMSeq seq = nf.LLVMSeq(Arrays.asList(objBitcast, call));
        v.addTranslation(n, nf.LLVMESeq(seq, ret));
        return super.translatePseudoLLVM(v);
    }
}
