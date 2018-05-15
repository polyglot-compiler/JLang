package polyllvm.extension;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.ConstructorInstance;
import polyglot.types.LocalInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMProcedureDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private static boolean noImplementation(ProcedureInstance pi) {
        return pi.flags().isNative() || pi.flags().isAbstract();
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();

        if (noImplementation(pi))
            return super.overrideTranslateLLVM(parent, v); // Ignore native and abstract methods.
        assert pi.container().isClass();

        String funcName = v.mangler.mangleProcName(pi);
        String debugName = pi.container().toClass().fullName() + "#" + pi.signature();

        Type retType = v.utils.erasedReturnType(pi);
        List<Type> argTypes = v.utils.erasedFormalTypes(pi);

        Runnable buildBody = () -> {

            // Initialize formals.
            for (int i = 0; i < n.formals().size(); ++i) {
                Formal formal = n.formals().get(i);
                LocalInstance li = formal.localInstance().orig();
                LLVMTypeRef typeRef = v.utils.toLL(formal.declType());
                LLVMValueRef alloca = v.utils.buildAlloca(formal.name(), typeRef);

                v.addTranslation(li, alloca);
                v.debugInfo.createParamVariable(v, formal, i, alloca);

                int idx = i + (pi.flags().isStatic() ? 0 : 1);
                LLVMValueRef param = LLVMGetParam(v.currFn(), idx);
                LLVMBuildStore(v.builder, param, alloca);
            }

            // If static method or constructor, make sure the container class has been initialized.
            // See JLS 7, section 12.4.1.
            if (pi.flags().isStatic() || pi instanceof ConstructorInstance) {
                // TODO: Make sure that classes are initialized in *native* static methods too.
                v.utils.buildClassLoadCheck(pi.container().toClass());
            }

            // Register as entry point if applicable.
            boolean isEntryPoint = n.name().equals("main")
                    && n.flags().isPublic()
                    && n.flags().isStatic()
                    && n.formals().size() == 1
                    && n.formals().iterator().next().declType().equals(v.ts.arrayOf(v.ts.String()));
            if (isEntryPoint) {
                String className = n.procedureInstance().container().toClass().fullName();
                v.addEntryPoint(v.currFn(), className);

                // Initialize the java.lang.String class at each entry point to avoid
                // the need for class loading before string literals.
                v.utils.buildClassLoadCheck(v.ts.String());
            }

            // Recurse to children.
            lang().visitChildren(n, v);
        };

        v.utils.buildFunc(n.position(), funcName, debugName, retType, argTypes, buildBody);

        return n;
    }
}
