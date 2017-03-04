package polyllvm.extension;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMConstructorCallExt extends PolyLLVMProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ConstructorCall n = (ConstructorCall) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        if (n.qualifier() != null) {
            throw new InternalCompilerError("Qualifier on this not supported yet (Java spec 15.8.4)");
        }

        v.debugInfo.emitLocation(n);

        LLVMValueRef thisArg;

        if (n.kind() == ConstructorCall.THIS) {
            thisArg = LLVMGetParam(v.currFn(), 0);
        }
        else if (n.kind() == ConstructorCall.SUPER) {
            LLVMTypeRef superType = LLVMUtils.typeRef(n.constructorInstance().container(), v);
            thisArg = LLVMBuildBitCast(v.builder, LLVMGetParam(v.currFn(), 0), superType, "cast_to_super");
        }
        else {
            throw new InternalCompilerError("Kind `" + n.kind()
                    + "` of constructor call not handled: " + n);
        }

        String mangledFuncName =
                PolyLLVMMangler.mangleProcedureName(n.constructorInstance());
        LLVMTypeRef constructorFuncTypeRef = LLVMUtils.methodType(n.constructorInstance().container(),
                v.typeSystem().Void(), n.constructorInstance().formalTypes(), v);
        LLVMValueRef function = LLVMUtils.getFunction(v.mod, mangledFuncName, constructorFuncTypeRef);
        LLVMValueRef[] args = Stream.concat(
                Stream.of(thisArg),
                n.arguments().stream().map(v::getTranslation)
        ).toArray(LLVMValueRef[]::new);
        LLVMValueRef procedureCall = LLVMUtils.buildProcedureCall(v, function, args);
        v.addTranslation(n, procedureCall);
        return n;
    }
}
