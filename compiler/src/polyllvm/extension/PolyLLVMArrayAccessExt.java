package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
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
        NodeFactory nf = v.nodeFactory();
        TypeSystem ts = v.typeSystem();
        Position pos = n.position();

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
                        v.getCurrentClass().type(),
                        /*fromClient*/ true);
                // TODO: Technically duplicates side-effects in n.index() if an exception is thrown.
                //       Need to find a cleaner  way to generate exceptions from the compiler.
                List<Expr> args = Collections.singletonList(n.index());
                Expr newInstance = nf.New(pos, nf.CanonicalTypeNode(pos, exceptionType), args)
                        .constructorInstance(constructor)
                        .type(exceptionType);
                Throw throwOutOfBounds = nf.Throw(pos, newInstance);
                throwOutOfBounds.visit(v);
            } catch (SemanticException e){
                throw new InternalCompilerError(e);
            }
        });

        return v.utils.buildGEP(base, offset);
    }
}
