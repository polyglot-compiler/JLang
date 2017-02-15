package polyllvm.extension;

import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.Constants;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        New n = (New) node();

        ConstructorInstance ci = n.constructorInstance();
        ReferenceType classtype = ci.container();
        int mallocSize = v.layouts(classtype).part2().size() * 8;
        translateWithSize(v, LLVMConstInt(LLVMInt64Type(), mallocSize, 0));
        return super.translatePseudoLLVM(v);
    }

    public void translateWithSize(PseudoLLVMTranslator v, LLVMValueRef size) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();

        ReferenceType classtype = ci.container();

        //Allocate space for the new object - need to get the size of the object
        LLVMValueRef mallocFunc = LLVMGetNamedFunction(v.mod, Constants.MALLOC);
        LLVMValueRef obj = LLVMUtils.buildMethodCall(v.builder, mallocFunc, size);

        //Bitcast object
        LLVMValueRef cast = LLVMBuildBitCast(v.builder, obj, LLVMUtils.typeRef(classtype, v), "obj_cast");
        //Set the Dispatch vector
        LLVMValueRef gep = LLVMUtils.buildGEP(v.builder, cast, 
                LLVMConstInt(LLVMInt32Type(), 0, 0), LLVMConstInt(LLVMInt32Type(), 0, 0));
        LLVMValueRef dvGlobal = LLVMUtils.getGlobal(v.mod, PolyLLVMMangler.dispatchVectorVariable(classtype), LLVMUtils.dvTypeRef(classtype,v));
        LLVMBuildStore(v.builder, dvGlobal, gep);

        //Call the constructor function
        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.constructorInstance());

        LLVMTypeRef constructorType = LLVMUtils.functionType(n.constructorInstance().container(), n.constructorInstance().formalTypes(), v);
        LLVMValueRef constructor = LLVMUtils.getFunction(v.mod, mangledFuncName, constructorType);
        LLVMUtils.buildProcedureCall(v.builder, constructor, cast);

        v.addTranslation(n, cast);
    }
}
