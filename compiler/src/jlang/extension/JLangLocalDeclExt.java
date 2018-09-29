//Copyright (C) 2018 Cornell University

package jlang.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import java.lang.Override;

import jlang.ast.JLangExt;
import jlang.types.JLangLocalInstance;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangLocalDeclExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        JLangLocalInstance li = (JLangLocalInstance) n.localInstance().orig();
        LLVMTypeRef typeRef = v.utils.toLL(n.declType());

        LLVMValueRef translation;
        if (li.isSSA()) {
            // If SSA, just forward the initializer value.
            if (n.init() == null)
                throw new InternalCompilerError(
                        "Variable marked SSA, but the declaration has no initial value");
            translation = v.getTranslation(n.init());
        }
        else {
            // Otherwise, allocate on the stack.
            translation = v.utils.buildAlloca(n.name(), typeRef);
            if (n.init() != null) {
                LLVMBuildStore(v.builder, v.getTranslation(n.init()), translation);
            }
        }

        // Declare debug information if this variable is visible to the user.
        if (!li.isTemp()) {
            v.debugInfo.createLocalVariable(v, n, translation);
        }

        v.addTranslation(li, translation);
        return super.leaveTranslateLLVM(v);
    }
}
