package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.util.SerialVersionUID;

public interface LLVMICmp extends LLVMCmp {
    /** LLVM Integer Comparison code. */
    public static class IConditionCode extends ConditionCode {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public IConditionCode(String name) {
            super(name);
        }
    }

    public static final IConditionCode eq = new IConditionCode("eq");
    public static final IConditionCode ne = new IConditionCode("ne");
    public static final IConditionCode ugt = new IConditionCode("ugt");
    public static final IConditionCode uge = new IConditionCode("uge");
    public static final IConditionCode ult = new IConditionCode("ult");
    public static final IConditionCode ule = new IConditionCode("ule");
    public static final IConditionCode sgt = new IConditionCode("sgt");
    public static final IConditionCode sge = new IConditionCode("sge");
    public static final IConditionCode slt = new IConditionCode("slt");
    public static final IConditionCode sle = new IConditionCode("sle");

}
