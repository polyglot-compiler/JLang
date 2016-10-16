package polyllvm.extension;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMClassBodyExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassBody n = (ClassBody) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        List<LLVMFunction> funcs = new ArrayList<>();
        List<LLVMFunctionDeclaration> funcDecls = new ArrayList<>();

        for (ClassMember cm : n.members()) {
            if (cm instanceof ProcedureDecl
                    && !((ProcedureDecl) cm).flags().isNative()) {
                ProcedureDecl pd = (ProcedureDecl) cm;
                LLVMFunction translated = (LLVMFunction) v.getTranslation(pd);
                funcs.add(translated);

                // TODO: This is not restrictive enough--we may need to check that the method
                //       is also public, static, and has the right signature.
                if (pd.name().equals("main")) {
                    // This is the entry point to the program.
                    // We emit an LLVM main function that calls into this one.
                    // TODO: Eventually we want the user to be able to choose the entry point.
                    funcs.add(TranslationUtils.createEntryPoint(
                            nf, v.typeSystem(), translated.name()));
                }
            }
            else if (cm instanceof ProcedureDecl
                    && ((ProcedureDecl) cm).flags().isNative()) {
                funcDecls.add((LLVMFunctionDeclaration) v.getTranslation(cm));
            }
            else {
                throw new InternalCompilerError("Could not translate member: " + cm);
            }
        }

        LLVMSourceFile llf = nf.LLVMSourceFile(null, null, funcs, funcDecls, null);
        v.addTranslation(n, llf);

        return super.translatePseudoLLVM(v);
    }
}
