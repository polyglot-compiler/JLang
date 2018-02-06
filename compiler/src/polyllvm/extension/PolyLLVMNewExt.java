package polyllvm.extension;

import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        New n = (New) node();

        ConstructorInstance ci = n.constructorInstance();
        int mallocSize =
                (v.objFields(ci.container()).size() + /*Allocate space for Header*/ Constants.OBJECT_FIELDS_OFFSET) * 8;
        translateWithSize(v, LLVMConstInt(LLVMInt64TypeInContext(v.context), mallocSize, 0));
        return super.leaveTranslateLLVM(v);
    }

    // Visits children.
    public void translateWithSize(LLVMTranslator v, LLVMValueRef size) {
        New n = (New) node();
        n.visitChildren(v);

        ConstructorInstance substC = n.constructorInstance();
        ReferenceType clazz = substC.container();


        v.debugInfo.emitLocation();

        //Allocate space for the new object - need to get the size of the object
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef obj = v.utils.buildFunCall(calloc, size);

        v.debugInfo.emitLocation(n);

        //Bitcast object
        LLVMValueRef obj_cast = LLVMBuildBitCast(v.builder, obj, v.utils.toLL(clazz), "obj_cast");
        //Set the Dispatch vector
        LLVMValueRef gep = v.utils.buildStructGEP(obj_cast, 0, Constants.DISPATCH_VECTOR_OFFSET);
        LLVMValueRef dvGlobal = v.utils.toCDVGlobal(clazz);
        LLVMBuildStore(v.builder, dvGlobal, gep);

        //Call the constructor function
        String mangledFuncName = v.mangler.mangleProcedureName(substC);

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(clazz, v.typeSystem().Void(), v.utils.formalsErasureLL(substC));
        LLVMValueRef func = v.utils.getFunction(v.mod, mangledFuncName, func_ty);
        // Bitcast the function so that the formal types are the types that
        // the arguments were cast to by MakeCastsExplicitVisitor. It is
        // needed due to potential mismatch between the types caused by
        // erasure.
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(clazz, v.typeSystem().Void(), substC.formalTypes());
        func = LLVMBuildBitCast(v.builder, func, v.utils.ptrTypeRef(func_ty_cast), "constructor_cast");

        LLVMValueRef[] args = Stream.concat(
                Stream.of(obj_cast), n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);
        v.utils.buildProcCall(func, args);

        v.addTranslation(n, obj_cast);
    }
}
