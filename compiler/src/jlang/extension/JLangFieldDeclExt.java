//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.types.FieldInstance;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangFieldDeclExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        FieldDecl n = (FieldDecl) node();

        // Initializers are desugared into standalone functions, so no need
        // initialize fields here. We only need to declare static fields as global variables.
        if (n.flags().isStatic()) {
            FieldInstance fi = n.fieldInstance();
            String mangledName = v.mangler.staticField(fi);
            LLVMTypeRef type = v.utils.toLL(n.declType());
            LLVMValueRef global = v.utils.getGlobal(mangledName, type);
	    LLVMValueRef val = v.utils.buildConst(n.init(), type, lang());

	    // TODO: Could use LLVMSetGlobalConstant for constant final fields.
	    if (val != null) {
		LLVMSetExternallyInitialized(global, 0);
		LLVMSetInitializer(global, val);
		LLVMSetLinkage(global, LLVMLinkOnceODRLinkage);
	    } else {
		LLVMSetInitializer(global, LLVMConstNull(type));
	    }
        }

        return super.leaveTranslateLLVM(v);
    }
}
