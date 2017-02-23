package polyllvm.extension;

import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMSetInitializer;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMFieldDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        FieldDecl n = (FieldDecl) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        // Only static field declarations need a translation.
        if (n.flags().isStatic()) {
            ReferenceType classType = v.getCurrentClass().type().toReference();
            String mangledName = PolyLLVMMangler.mangleStaticFieldName(classType, n);
            LLVMValueRef global = LLVMUtils.getGlobal(v.mod,
                    mangledName, LLVMUtils.typeRef(n.type().type(), v));
            if(n.init() == null){
                LLVMSetInitializer(global, LLVMUtils.defaultValue(n.type().type(), v));
            } else {
                LLVMSetInitializer(global, v.getTranslation(n.init()));
            }
            v.addTranslation(n, global);
        }

        return super.translatePseudoLLVM(v);
    }
}
