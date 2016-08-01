package polyllvm.extension;

import java.util.Arrays;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMSourceFileExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        SourceFile n = (SourceFile) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMSourceFile llf =
                v.nodeFactory().LLVMSourceFile(n.position().file(),
                                               n.source(),
                                               null,
                                               null,
                                               null);
        for (TopLevelDecl tld : n.decls()) {
            LLVMSourceFile sf = (LLVMSourceFile) v.getTranslation(tld);
            llf = llf.merge(sf);
        }
        for (LLVMGlobalDeclaration d : v.globalDeclarations()) {
            llf = llf.appendGlobal(d);
        }
        for (LLVMFunction ctorFunc : v.ctorFunctions()) {
            llf = llf.addCtor(ctorFunc);
        }

        //Add malloc function: i8* @malloc(i64)
        LLVMIntType i64Type = nf.LLVMIntType(64);
        List<LLVMArgDecl> argDecls =
                Arrays.asList(nf.LLVMArgDecl(i64Type, "size"));
        LLVMPointerType llvmPointerType = nf.LLVMPointerType(nf.LLVMIntType(8));
        LLVMFunctionDeclaration malloc =
                nf.LLVMFunctionDeclaration(PolyLLVMConstants.MALLOC,
                                           argDecls,
                                           llvmPointerType);

        llf = llf.appendFunctionDeclaration(malloc);

        for (LLVMCall c : v.getStaticCalls()) {
            LLVMFunctionDeclaration callFuncDecl = c.functionDeclaration(nf);
            if (!llf.containsFunction(callFuncDecl)) {
                llf = llf.appendFunctionDeclaration(callFuncDecl);
            }
        }

        return llf;
    }
}
