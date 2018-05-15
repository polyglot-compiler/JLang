package polyllvm.extension;

import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMNewExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        New n = (New) node();
        if (n.qualifier() != null)
            throw new InternalCompilerError("Qualifier should have been desugared");

        // This is an anonymous class! Initialize class data structures.
        if (n.body() != null) {
            PolyLLVMClassDeclExt.initClassDataStructures(v, n.type().toClass(), n.body());
        }

        // Most of the translation happens in this super call.
        return super.leaveTranslateLLVM(v);
    }

    /** Translate with specified arguments and size. */
    public static LLVMValueRef translateWithArgsAndSize(
            LLVMTranslator v, LLVMValueRef[] args, LLVMValueRef size, ConstructorInstance ci) {
        // TODO: This duplicates code elsewhere. Restructure.

        ReferenceType clazz = ci.container();

        // Allocate space for the new object.
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef obj = v.utils.buildFunCall(calloc, size);

        // Bitcast object
        LLVMValueRef objCast = LLVMBuildBitCast(v.builder, obj, v.utils.toLL(clazz), "obj_cast");

        // Set the Dispatch vector
        // TODO: Could probably do this inside the constructor function instead?
        LLVMValueRef gep = v.obj.buildDispatchVectorElementPtr(objCast, clazz);
        LLVMValueRef dvGlobal = v.dv.getDispatchVectorFor(clazz);
        LLVMBuildStore(v.builder, dvGlobal, gep);

        // Call the constructor function
        String mangledFuncName = v.mangler.mangleProcName(ci);

        LLVMTypeRef funcType = v.utils.toLL(ci);
        LLVMValueRef funcPtr = v.utils.getFunction(mangledFuncName, funcType);

        // Bitcast the function so that the formal types are the types that
        // the arguments were cast to by DesugarImplicitConversions. It is
        // needed due to potential mismatch between the types caused by erasure.
        LLVMTypeRef funcTyCast = v.utils.toLLFuncTy(
                clazz, v.ts.Void(), ci.formalTypes());
        funcPtr = LLVMBuildBitCast(v.builder, funcPtr, v.utils.ptrTypeRef(funcTyCast), "cast");

        LLVMValueRef[] llvmArgs = Stream.concat(
                Stream.of(objCast), Arrays.stream(args))
                .toArray(LLVMValueRef[]::new);
        v.utils.buildProcCall(funcPtr, llvmArgs);

        return objCast;
    }

    @Override
    protected LLVMValueRef buildReceiverArg(LLVMTranslator v) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();
        ReferenceType clazz = ci.container();
        LLVMValueRef size = v.obj.sizeOf(ci.container());

        // Allocate space for the new object.
        LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
        LLVMValueRef rawPtr = v.utils.buildFunCall(calloc, size);

        // Bitcast object
        LLVMValueRef obj = LLVMBuildBitCast(v.builder, rawPtr, v.utils.toLL(clazz), "cast.new");

        // Set the Dispatch vector
        LLVMValueRef gep = v.obj.buildDispatchVectorElementPtr(obj, clazz);
        LLVMValueRef dvGlobal = v.dv.getDispatchVectorFor(clazz);
        LLVMBuildStore(v.builder, dvGlobal, gep);

        // The receiver should be the eventual result of translating this node.
        v.addTranslation(n, obj);

        return obj;
    }
}
