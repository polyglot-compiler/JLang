package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.annotation.Const;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.types.reflect.Constant;
import polyglot.util.CollectionUtil;
import polyglot.util.Pair;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable.VarKind;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.Constants;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        New n = (New) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        ConstructorInstance ci = n.constructorInstance();
        ReferenceType classtype = ci.container();
        int mallocSize = v.layouts(classtype).part2().size() * 8;
        translateWithSize(v, LLVMConstInt(LLVMInt64Type(), mallocSize, 0));
        return super.translatePseudoLLVM(v);
    }

    public void translateWithSize(PseudoLLVMTranslator v, LLVMValueRef size) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();

        PolyLLVMNodeFactory nf = v.nodeFactory();

        ReferenceType classtype = ci.container();

        System.out.println("TYPE OF class "+classtype+": " + LLVMPrintTypeToString(LLVMGetTypeByName(v.mod,PolyLLVMMangler.classTypeName(classtype))).getString());
        ;

        //Allocate space for the new object - need to get the size of the object
        LLVMValueRef mallocFunc = LLVMGetNamedFunction(v.mod, Constants.MALLOC);
        LLVMValueRef obj = LLVMUtils.buildMethodCall(v.builder, mallocFunc, size);

        //Bitcast object
        LLVMValueRef cast = LLVMBuildBitCast(v.builder, obj, LLVMUtils.typeRef(classtype, v.mod), "obj_cast");
        LLVMTypeRef typeOf = LLVMTypeOf(cast);
        System.out.println("TYPE OF CAST: " + LLVMPrintTypeToString(typeOf).getString());
        //Set the Dispatch vector
        LLVMValueRef gep = LLVMUtils.buildGEP(v.builder, cast, 
                LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 0, 0));
        LLVMValueRef dvGlobal = LLVMGetNamedGlobal(v.mod, PolyLLVMMangler.dispatchVectorVariable(classtype));
        LLVMBuildStore(v.builder, dvGlobal, gep);

        //Call the constructor function
        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.constructorInstance());

        LLVMTypeRef constructorType = LLVMUtils.functionType(n.constructorInstance().container(), n.constructorInstance().formalTypes(), v.mod);
        LLVMValueRef constructor = LLVMUtils.getFunction(v.mod, mangledFuncName, constructorType);
        LLVMUtils.buildProcedureCall(v.builder, constructor, cast);

        v.addTranslation(n, obj);
    }

    @SafeVarargs
    private final <T> List<T> list(T... ts) {
        ArrayList<T> list = new ArrayList<>();
        for (T element : ts) {
            list.add(element);
        }
        return list;
    }

}
