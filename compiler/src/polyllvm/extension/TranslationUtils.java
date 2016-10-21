package polyllvm.extension;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMTypeUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TranslationUtils {

    private TranslationUtils() {}

    /**
     * Returns an LLVM main function that calls into the specified Java entry point.
     */
    public static LLVMFunction createEntryPoint(PolyLLVMNodeFactory nf,
                                                TypeSystem ts,
                                                String entryFunc) {

        // Build the function type for `void main(String[] args)`.
        Type strType;
        try {
            strType = ts.typeForName("java.lang.String");
        } catch (SemanticException e) {
            throw new InternalCompilerError("Need java.lang.String for entry point", e);
        }
        Type strArrType = ts.arrayOf(strType);
        LLVMTypeNode llvmStrArrType = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, strArrType);
        LLVMTypeNode entryFuncType = nf.LLVMFunctionType(
                Collections.singletonList(llvmStrArrType),
                nf.LLVMVoidType());

        // Build the call to the entry function.
        // TODO: Convert `char** args` into `String[] args`.
        List<Pair<LLVMTypeNode, LLVMOperand>> entryArgs = Collections.singletonList(
                new Pair<>(llvmStrArrType, nf.LLVMNullLiteral(llvmStrArrType)));
        LLVMInstruction callEntry = nf.LLVMCall(
                nf.LLVMVariable(entryFunc, entryFuncType, LLVMVariable.VarKind.GLOBAL),
                entryArgs,
                nf.LLVMVoidType());

        // Build the LLVM main function.
        List<LLVMArgDecl> mainArgs = Arrays.asList(
                nf.LLVMArgDecl(nf.LLVMIntType(32), "argv"),
                nf.LLVMArgDecl(nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))), "args"));
        LLVMBlock body = nf.LLVMBlock(Arrays.asList(
                callEntry,
                nf.LLVMRet()));
        return nf.LLVMFunction("main", mainArgs, nf.LLVMVoidType(), body);
    }
}
