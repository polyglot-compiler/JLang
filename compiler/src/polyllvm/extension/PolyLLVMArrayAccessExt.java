package polyllvm.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Node;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug location.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "arr_load");
        v.addTranslation(n, load);
        return n;
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        // Return a pointer to the appropriate element in the array, and emit a bounds check.
        ArrayAccess n = (ArrayAccess) node();
        TypeSystem ts = v.typeSystem();

        n.array().visit(v);
        n.index().visit(v);

        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef base = v.utils.buildJavaArrayBase(arr, n.type());
        LLVMValueRef offset = v.getTranslation(n.index());

        LLVMValueRef lenPtr = v.utils.buildStructGEP(arr, 0, Constants.ARR_LEN_OFFSET);
        LLVMValueRef len = LLVMBuildLoad(v.builder, lenPtr, "len");
        LLVMValueRef zero = LLVMConstNull(v.utils.toLL(n.index().type()));
        LLVMValueRef boundsCheck = LLVMBuildOr(
                v.builder,
                LLVMBuildICmp(v.builder, LLVMIntSLT, offset, zero, "lt_zero"),
                LLVMBuildICmp(v.builder, LLVMIntSGE, offset, len, "ge_len"),
                "bounds_check"
        );

        PolyLLVMIfExt.buildIf(v, boundsCheck, () -> {
            try {
                String exceptionName = "java.lang.ArrayIndexOutOfBoundsException";
                ClassType exceptionType = (ClassType) ts.typeForName(exceptionName);
                List<? extends Type> formalParams = Collections.singletonList(ts.Int());
                ConstructorInstance constructor = ts.findConstructor(
                        exceptionType,
                        formalParams,
                        ts.Object(),
                        /*fromClient*/ true);

                // We are careful here to reuse the translation of n.index() when throwing the
                // exception, since if we re-translated n.index() we would duplicate side effects.
                LLVMValueRef newInstance = PolyLLVMNewExt.translateWithArgs(
                        v, new LLVMValueRef[] {offset}, constructor);
                PolyLLVMThrowExt.buildThrow(v, newInstance);
            } catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
        });

        return v.utils.buildGEP(base, offset);
    }
}
