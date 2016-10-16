package polyllvm.ast.PseudoLLVM;

import polyglot.frontend.Source;

import java.util.List;

public interface LLVMSourceFile extends LLVMNode {

    Source source();

    String fileName();

    LLVMSourceFile merge(LLVMSourceFile s);

    LLVMSourceFile fileName(String s);

    LLVMSourceFile source(Source source);

    LLVMSourceFile functions(List<LLVMFunction> functions);

    LLVMSourceFile appendFunction(LLVMFunction f);

    LLVMSourceFile functionDeclarations(
            List<LLVMFunctionDeclaration> funcdecls);

    LLVMSourceFile appendFunctionDeclaration(LLVMFunctionDeclaration fd);

    LLVMSourceFile globals(List<LLVMGlobalDeclaration> globals);

    LLVMSourceFile appendGlobal(LLVMGlobalDeclaration gd);

    LLVMSourceFile addCtor(LLVMFunction ctorFunc);

    boolean containsFunction(LLVMFunctionDeclaration functionDeclaration);

}
