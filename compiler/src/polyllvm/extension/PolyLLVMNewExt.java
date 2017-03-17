package polyllvm.extension;

import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.util.Constants;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.LLVMTranslator;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        New n = (New) node();

        ConstructorInstance ci = n.constructorInstance();
        ReferenceType classtype = ci.container();
        int mallocSize =
                (v.layouts(classtype).part2().size() + /*Allocate space for DV ptr*/ 1) * 8;
        translateWithSize(v, LLVMConstInt(LLVMInt64TypeInContext(v.context), mallocSize, 0));
        return super.translatePseudoLLVM(v);
    }

    public void translateWithSize(LLVMTranslator v, LLVMValueRef size) {
        New n = (New) node();
        ConstructorInstance origCi = n.constructorInstance();
        ConstructorInstance ci = (ConstructorInstance) v.jl5Utils.translateMemberInstance(origCi);

        ReferenceType classtype = ci.container();


        v.debugInfo.emitLocation();

        //Allocate space for the new object - need to get the size of the object
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef obj = v.utils.buildMethodCall(calloc, size);

        v.debugInfo.emitLocation(n);

        //Bitcast object
        LLVMValueRef cast = LLVMBuildBitCast(v.builder, obj, v.utils.typeRef(classtype), "obj_cast");
        //Set the Dispatch vector
        LLVMValueRef gep = v.utils.buildStructGEP(cast, 0, 0);
        LLVMValueRef dvGlobal = v.utils.getDvGlobal(classtype);
        LLVMBuildStore(v.builder, dvGlobal, gep);

        //Call the constructor function
        String mangledFuncName =
                v.mangler.mangleProcedureName(ci);


        LLVMTypeRef constructorType = v.utils.methodType(classtype,
                v.typeSystem().Void(), ci.formalTypes());
        LLVMValueRef constructor = v.utils.getFunction(v.mod, mangledFuncName, constructorType);

        LLVMValueRef[] constructorArgs = Stream.concat(
                Stream.of(cast), n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);
        v.utils.buildProcedureCall(constructor, constructorArgs);

        v.addTranslation(n, cast);
    }
}
