package polyllvm.extension;

import polyglot.ast.FieldDecl;
import polyglot.ast.Lit;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMFieldDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        FieldDecl n = (FieldDecl) node();

        // Only static field declarations need a translation.
        if (n.flags().isStatic()) {
            ReferenceType classType = v.getCurrentClass().type().toReference();
            String mangledName = v.mangler.mangleStaticFieldName(classType, n);
            LLVMTypeRef type = v.utils.typeRef(n.type().type());
            LLVMValueRef global = v.utils.getGlobal(v.mod, mangledName, type);
            if (n.init() == null) {
                // LLVMConstNull will give zero for any type, including numeric and pointer types.
                LLVMSetInitializer(global, LLVMConstNull(type));
            } else {
                if (n instanceof Lit) {
                    n.init().visit(v);
                    LLVMValueRef val = v.getTranslation(n.init());
                    assert LLVMIsConstant(val) == 1 : "literal should translate to a constant";
                    LLVMSetInitializer(global, val);
                } else {
                    LLVMSetInitializer(global, LLVMConstNull(type));
                    v.utils.buildCtor(n, () -> {
                        n.init().visit(v);
                        LLVMValueRef val = v.getTranslation(n.init());
                        LLVMBuildStore(v.builder, val, global);
                        return global;
                    });
                }
            }
            v.addTranslation(n, global);
        }

        return super.translatePseudoLLVM(v);
    }
}
