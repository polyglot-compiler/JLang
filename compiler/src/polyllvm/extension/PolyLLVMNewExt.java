package polyllvm.extension;

import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private LLVMValueRef mallocSize(LLVMTranslator v, ConstructorInstance ci) {
        // TODO: This assumes all fields require one word.
        int bytes = v.utils.llvmPtrSize()
                * (v.objFields(ci.container()).size() + /*header*/ Constants.OBJECT_FIELDS_OFFSET);
        return LLVMConstInt(LLVMInt64TypeInContext(v.context), bytes, /*signExtend*/ 0);
    }

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();
        translateWithSize(v, mallocSize(v, ci));
        return super.leaveTranslateLLVM(v);
    }

    public void translateWithSize(LLVMTranslator v, LLVMValueRef size) {
        New n = (New) node();
        n.visitChildren(v);
        LLVMValueRef[] args = n.arguments().stream()
                .map(v::getTranslation)
                .toArray(LLVMValueRef[]::new);
        translateWithArgsAndSize(v, args, size);
    }

    public void translateWithArgs(LLVMTranslator v, LLVMValueRef[] args) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();
        translateWithArgsAndSize(v, args, mallocSize(v, ci));
    }

    public void translateWithArgsAndSize(LLVMTranslator v, LLVMValueRef[] args, LLVMValueRef size) {
        New n = (New) node();

        ConstructorInstance substC = n.constructorInstance();
        ReferenceType clazz = substC.container();

        // Allocate space for the new object.
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef obj = v.utils.buildFunCall(calloc, size);

        // Bitcast object
        LLVMValueRef objCast = LLVMBuildBitCast(v.builder, obj, v.utils.toLL(clazz), "obj_cast");

        // Set the Dispatch vector
        LLVMValueRef gep = v.utils.buildStructGEP(objCast, 0, Constants.DISPATCH_VECTOR_OFFSET);
        LLVMValueRef dvGlobal = v.utils.toCDVGlobal(clazz);
        LLVMBuildStore(v.builder, dvGlobal, gep);

        // Call the constructor function
        String mangledFuncName = v.mangler.mangleProcName(substC);

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(
                clazz, v.typeSystem().Void(), v.utils.formalsErasureLL(substC));
        LLVMValueRef func = v.utils.getFunction(v.mod, mangledFuncName, func_ty);

        // Bitcast the function so that the formal types are the types that
        // the arguments were cast to by MakeCastsExplicitVisitor. It is
        // needed due to potential mismatch between the types caused by erasure.
        LLVMTypeRef funcTyCast = v.utils.toLLFuncTy(
                clazz, v.typeSystem().Void(), substC.formalTypes());
        func = LLVMBuildBitCast(v.builder, func, v.utils.ptrTypeRef(funcTyCast), "cast");

        LLVMValueRef[] llvmArgs = Stream.concat(
                Stream.of(objCast), Arrays.stream(args))
                .toArray(LLVMValueRef[]::new);
        v.utils.buildProcCall(func, llvmArgs);

        v.addTranslation(n, objCast);
    }
}
