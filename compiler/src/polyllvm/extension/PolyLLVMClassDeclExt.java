package polyllvm.extension;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

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
        List<? extends ReferenceType> interfaces = v.allInterfaces(n.type());
        LLVMValueRef[] dvMethods;
        if(interfaces.size() > 0){
            LLVMValueRef itGlobal =  LLVMConstBitCast(LLVMUtils.getItGlobal(v, interfaces.get(0), n.type()),
                    LLVMUtils.ptrTypeRef(LLVMInt8Type()));
            dvMethods = LLVMUtils.dvMethods(v, n.type(), itGlobal);

        } else {
            dvMethods = LLVMUtils.dvMethods(v, n.type());
        }

        LLVMValueRef dvGlobal = LLVMUtils.getDvGlobal(v, n.type());
        LLVMValueRef initStruct = LLVMUtils.buildConstStruct(dvMethods);
        LLVMSetInitializer(dvGlobal, initStruct);

        //Setup the Interface Tables for this class
        for (int i=0; i< interfaces.size(); i++){
            ReferenceType it = interfaces.get(i);
            String interfaceTableVar = PolyLLVMMangler.InterfaceTableVariable(n.type(), it);
            LLVMTypeRef interfaceTableType = LLVMUtils.dvTypeRef(it, v);
            LLVMValueRef itGlobal = LLVMUtils.getGlobal(v.mod, interfaceTableVar, interfaceTableType);

            LLVMValueRef next;
            if(i == interfaces.size()-1){ // Last Interface next pointer points to null
                next = LLVMConstNull(LLVMUtils.ptrTypeRef(LLVMInt8Type()));
            } else {
                next = LLVMConstBitCast(LLVMUtils.getItGlobal(v, interfaces.get(i+1), n.type()), LLVMUtils.ptrTypeRef(LLVMInt8Type()));
            }
            LLVMValueRef[] itMethods = LLVMUtils.itMethods(v, it, n.type(), next);

            String s = it.toString();
            LLVMTypeRef stringType = LLVMArrayType(LLVMInt8Type(), s.length() + 1);
            LLVMValueRef interfaceName = LLVMUtils.getGlobal(v.mod,
                    PolyLLVMMangler.interfaceStringVariable(it), stringType);
            LLVMSetInitializer(interfaceName, LLVMConstString(s, s.length(), /*Don't null terminate*/ 0));

            itMethods[1] = LLVMUtils.constGEP(interfaceName,0,0);
            LLVMValueRef itStruct = LLVMUtils.buildConstStruct(itMethods);
            LLVMSetInitializer(itGlobal, itStruct);

        }

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
