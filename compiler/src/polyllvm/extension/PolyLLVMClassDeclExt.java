package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        v.enterClass((ClassDecl) node());
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();

        // External class object declarations.
        Type superType = n.type().superType();
        while (superType != null) {
            ClassObjects.classIdDeclRef(v.mod, superType.toReference(), /* extern */ true);
            superType = superType.toReference().superType();
        }
        n.interfaces().stream().map(tn -> tn.type().toReference())
                               .map(rt -> ClassObjects.classIdDeclRef(v.mod, rt, /* extern */ true));

        // Class object for this class.
        ClassObjects.classIdDeclRef(v.mod, n.type().toReference(), /* extern */ false);
        ClassObjects.classObjRef(v.mod, n.type().toReference());


        //Set the DV for this class.
        LLVMValueRef dvGlobal = LLVMUtils.getDvGlobal(v, n.type());
        LLVMValueRef[] dvMethods = LLVMUtils.dvMethods(v, n.type());
        LLVMValueRef initStruct = LLVMUtils.buildConstStruct(dvMethods);
        LLVMSetInitializer(dvGlobal, initStruct);

        v.leaveClass();
        return super.translatePseudoLLVM(v);
    }


    @Override
    public Node overrideTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        if (n.flags().isInterface()) {
            // Interfaces need only declare a class id.
            ClassObjects.classIdDeclRef(v.mod, n.type().toReference(), /* extern */ false);
            return n;
        }
        return super.overrideTranslatePseudoLLVM(v);

    }
}
