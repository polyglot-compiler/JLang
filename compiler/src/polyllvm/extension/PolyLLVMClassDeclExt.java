package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.*;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        v.enterClass((ClassDecl) node());
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        List<LLVMFunction> funcs = new ArrayList<>();
        List<LLVMFunctionDeclaration> funcDecls = new ArrayList<>();
        List<LLVMGlobalDeclaration> globals = new ArrayList<>();

        for (ClassMember cm : n.body().members()) {
            if (cm instanceof ProcedureDecl) {
                ProcedureDecl pd = (ProcedureDecl) cm;
                if (pd.flags().isNative() || pd.flags().isAbstract()) {
                    // Native or Abstract procedure declarations.
                    funcDecls.add((LLVMFunctionDeclaration) v.getTranslation(cm));
                } else {
                    // Normal procedure declarations.
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
            }
            else if (cm instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) cm;
                if (fd.flags().isStatic()) {
                    // Static field declarations.
                    globals.add((LLVMGlobalVarDeclaration) v.getTranslation(fd));
                }
            }
            else {
                throw new InternalCompilerError("Could not translate member: " + cm);
            }
        }

        // External class object declarations.
        // TODO: Will interfaces have class objects?
        Type superType = n.type().superType();
        while (superType != null) {
            LLVMGlobalVarDeclaration decl =
                    ClassObjects.classIdDecl(nf, superType.toReference(), /* extern */ true);
            v.addStaticVarReferenced(decl.name(), decl);
            superType = superType.toReference().superType();
        }
        n.interfaces().stream().map(tn -> tn.type().toReference())
                               .map(rt -> ClassObjects.classIdDecl(nf, rt, /* extern */ true))
                               .forEach(decl -> v.addStaticVarReferenced(decl.name(), decl));

        // Class object for this class.
        globals.add(ClassObjects.classIdDecl(nf, n.type().toReference(), /* extern */ false));
        globals.add(ClassObjects.classObj(nf, n.type().toReference()));

        LLVMSourceFile llf = nf.LLVMSourceFile(null, null, funcs, funcDecls, globals);
        v.addTranslation(n, llf);
        v.leaveClass();
        return super.translatePseudoLLVM(v);
    }


    @Override
    public Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        if(n.flags().isInterface()){
            return n;
        }
        return super.overrideTranslatePseudoLLVM(v);

    }
}
