package polyllvm.extension;

import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.PseudoLLVMTranslator;

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
            LLVMGlobalVarDeclaration globalDecl = nf.LLVMGlobalVarDeclaration(
                    mangledName,
                    /* isExtern */ false,
                    LLVMGlobalVarDeclaration.GLOBAL,
                    PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.declType()),
                    (LLVMOperand) v.getTranslation(n.init()));
            v.addTranslation(n, globalDecl);
        }

        return super.translatePseudoLLVM(v);
    }
}
