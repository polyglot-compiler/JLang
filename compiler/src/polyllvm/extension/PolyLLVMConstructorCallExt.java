package polyllvm.extension;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMConstructorCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ConstructorCall n = (ConstructorCall) node();
        ConstructorInstance substC = n.constructorInstance();
        ReferenceType supc = substC.container();

        if (n.qualifier() != null) {
            throw new InternalCompilerError(
                    "Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        LLVMValueRef thisArg;

        if (n.kind() == ConstructorCall.THIS) {
            thisArg = LLVMGetParam(v.currFn(), 0);
        } else if (n.kind() == ConstructorCall.SUPER) {
            thisArg = LLVMBuildBitCast(
                    v.builder, LLVMGetParam(v.currFn(), 0), v.utils.toLL(supc), "cast.super");
        } else {
            throw new InternalCompilerError(n.kind().toString() + " not handled: " + n);
        }

        String mangledFuncName = v.mangler.mangleProcName(substC);

        LLVMTypeRef func_ty = v.utils.toLLFuncTy(supc, v.typeSystem().Void(),
                v.utils.formalsErasureLL(substC));
        LLVMValueRef func = v.utils.getFunction(v.mod, mangledFuncName,
                func_ty);
        LLVMTypeRef func_ty_cast = v.utils.toLLFuncTy(supc,
                v.typeSystem().Void(), substC.formalTypes());
        // Bitcast the function so that the formal types are the types that the
        // arguments were cast to by ExplicitCastsVisitor
        func = LLVMBuildBitCast(v.builder, func, v.utils.ptrTypeRef(func_ty_cast), "cast");
        LLVMValueRef[] args = Stream
                .concat(Stream.of(thisArg),
                        n.arguments().stream().map(v::getTranslation))
                .toArray(LLVMValueRef[]::new);
        LLVMValueRef procedureCall = v.utils.buildProcCall(func, args);
        v.addTranslation(n, procedureCall);
        return n;
    }
}
