package polyllvm.extension;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMClassBodyExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassBody n = (ClassBody) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMSourceFile llf = nf.LLVMSourceFile(null, null, null, null, null);
        for (ClassMember cm : n.members()) {
//            if (cm instanceof ConstructorDecl) {
//                System.out.println("Working on constructors: " + cm);
//                continue;
//            }

            if (cm instanceof ProcedureDecl
                    && !((ProcedureDecl) cm).flags().isNative()) {
                llf = llf.appendFunction((LLVMFunction) v.getTranslation(cm));
            }
            else if (cm instanceof ProcedureDecl
                    && ((ProcedureDecl) cm).flags().isNative()) {
                llf = llf.appendFunctionDeclaration((LLVMFunctionDeclaration) v.getTranslation(cm));
            }
            else {
                System.out.println("Could not translate " + cm
                        + " - Skipping it.");
            }
        }

        v.addTranslation(n, llf);

        return super.translatePseudoLLVM(v);
    }
}
