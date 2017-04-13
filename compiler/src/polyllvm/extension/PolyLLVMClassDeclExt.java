package polyllvm.extension;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LLVMTranslator enterTranslatePseudoLLVM(LLVMTranslator v) {
        v.enterClass((ClassDecl) node());
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();

        // External class object declarations.
        Type superType = n.type().superType();
        while (superType != null) {
            v.classObjs.classIdDeclRef(superType.toReference(), /* extern */ true);
            superType = superType.toReference().superType();
        }
        n.interfaces().stream().map(tn -> tn.type().toReference())
                               .map(rt -> v.classObjs.classIdDeclRef(rt, /* extern */ true));

        // Class object for this class.
        v.classObjs.classIdDeclRef(n.type().toReference(), /* extern */ false);
        v.classObjs.classObjRef(n.type().toReference());

        List<? extends ReferenceType> interfaces = v.allInterfaces(n.type());

        if(!n.flags().isAbstract()) {
            //Set the DV for this class.
            LLVMValueRef[] dvMethods;
            if (interfaces.size() > 0) {
                LLVMValueRef itGlobal = LLVMConstBitCast(v.utils.getItGlobal(interfaces.get(0), n.type()),
                        v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)));
                dvMethods = v.utils.dvMethods(n.type(), itGlobal);
            } else {
                dvMethods = v.utils.dvMethods(n.type());
            }

            LLVMValueRef dvGlobal = v.utils.getDvGlobal(n.type());
            LLVMValueRef initStruct = v.utils.buildConstStruct(dvMethods);
            LLVMSetInitializer(dvGlobal, initStruct);
        }

        //Setup the Interface Tables for this class
        for (int i=0; i< interfaces.size(); i++) {
            ReferenceType it = interfaces.get(i);
            String interfaceTableVar = v.mangler.InterfaceTableVariable(n.type(), it);
            LLVMTypeRef interfaceTableType = v.utils.dvTypeRef(it);
            LLVMValueRef itGlobal = v.utils.getGlobal(v.mod, interfaceTableVar, interfaceTableType);

            LLVMValueRef next;
            if (i == interfaces.size()-1) { // Last Interface next pointer points to null
                next = LLVMConstNull(v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)));
            } else {
                next = LLVMConstBitCast(v.utils.getItGlobal(interfaces.get(i+1), n.type()), v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)));
            }
            LLVMValueRef[] itMethods = v.utils.itMethods(it, n.type(), next);

            String s = it.toString();
            LLVMTypeRef stringType = LLVMArrayType(LLVMInt8TypeInContext(v.context), s.length() + 1);
            LLVMValueRef interfaceName = v.utils.getGlobal(v.mod,
                    v.mangler.interfaceStringVariable(it), stringType);
            LLVMSetInitializer(interfaceName, LLVMConstString(s, s.length(), /*Don't null terminate*/ 0));
            LLVMSetLinkage(interfaceName, LLVMLinkOnceODRLinkage);

            itMethods[1] = v.utils.constGEP(interfaceName,0,0);
            LLVMValueRef itStruct = v.utils.buildConstStruct(itMethods);
            LLVMSetInitializer(itGlobal, itStruct);

        }

        v.leaveClass();
        return super.translatePseudoLLVM(v);
    }


    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        if (n.flags().isInterface()) {
            // Interfaces need only declare a class id.
            v.classObjs.classIdDeclRef(n.type().toReference(), /* extern */ false);
            return n;
        }
        return super.overrideTranslatePseudoLLVM(v);

    }
}
