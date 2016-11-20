package polyllvm.ast.PseudoLLVM;

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

public interface LLVMGlobalVarDeclaration extends LLVMGlobalDeclaration {
    /** LLVM Global variable kind - global or constant. */
    public static class GlobalVariableKind extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public GlobalVariableKind(String name) {
            super(name);
        }
    }

    String name();

    boolean isExtern();

    public static final GlobalVariableKind GLOBAL =
            new GlobalVariableKind("global");
    public static final GlobalVariableKind CONSTANT =
            new GlobalVariableKind("constant");

}
