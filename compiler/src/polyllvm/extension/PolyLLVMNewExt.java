package polyllvm.extension;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.CollectionUtil;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMNewExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();
        List<Expr> args = n.arguments();

        PolyLLVMNodeFactory nf = v.nodeFactory();

        ReferenceType classtype = ci.container();
        LLVMTypeNode typeNode =
                PolyLLVMTypeUtils.polyLLVMTypeNode(nf, classtype);
        System.out.println("Type node for the class is: " + typeNode);

        //Allocate space for the new object - need to get the size of the object
        LLVMVariable mallocRet =
                PolyLLVMFreshGen.freshLocalVar(nf,
                                               nf.LLVMPointerType(nf.LLVMIntType(8)));

        LLVMVariable mallocFunction = nf.LLVMVariable(PolyLLVMConstants.MALLOC,
                                                      nf.LLVMPointerType(nf.LLVMIntType(8)),
                                                      VarType.GLOBAL);
        List<Pair<LLVMTypeNode, LLVMOperand>> arguments =
                CollectionUtil.list(new Pair<LLVMTypeNode, LLVMOperand>(nf.LLVMIntType(64),
                                                                        nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                          v.layouts(classtype)
                                                                                           .part2()
                                                                                           .size() * 8)));
        LLVMTypeNode retType = nf.LLVMPointerType(nf.LLVMIntType(8));
        LLVMCall mallocCall = nf.LLVMCall(mallocFunction, arguments, retType)
                                .result(mallocRet);

        //Bitcast object
        LLVMVariable newObject = PolyLLVMFreshGen.freshLocalVar(nf, typeNode);
        LLVMConversion conversion =
                nf.LLVMConversion(LLVMConversion.BITCAST,
                                  newObject,
                                  retType,
                                  mallocRet,
                                  typeNode);

        //Set the Dispatch vector
        LLVMTypeNode dvTypeNode =
                PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(v,
                                                                     classtype);
        LLVMTypedOperand index0 =
                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(32), 0),
                                    nf.LLVMIntType(32));
        List<LLVMTypedOperand> gepIndexList =
                CollectionUtil.list(index0, index0);
        LLVMVariable gepResult =
                PolyLLVMFreshGen.freshLocalVar(nf,
                                               nf.LLVMPointerType(dvTypeNode));
        LLVMInstruction gep =
                nf.LLVMGetElementPtr(newObject, gepIndexList).result(gepResult);

        LLVMStore storeDVIntoObject =
                nf.LLVMStore(nf.LLVMPointerType(dvTypeNode),
                             nf.LLVMVariable(PolyLLVMMangler.dispatchVectorVariable(classtype),
                                             PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(v,
                                                                                                  classtype),
                                             VarType.GLOBAL),
                             gepResult);
        //Call the constructor function
        //TODO : Actually do this part

        List<LLVMInstruction> instrs =
                CollectionUtil.list(mallocCall,
                                    conversion,
                                    gep,
                                    storeDVIntoObject);

        v.addTranslation(n, nf.LLVMESeq(nf.LLVMSeq(instrs), newObject));

        return super.translatePseudoLLVM(v);
    }

}
