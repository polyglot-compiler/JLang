package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

public interface LLVMCmp extends LLVMBinaryOperandInstruction {
    /** LLVM Comparison code. */
    public static class ConditionCode extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public ConditionCode(String name) {
            super(name);
        }
    }

//    public static final ConditionCode eq = new ConditionCode("eq");
//    public static final ConditionCode ne = new ConditionCode("ne");
//    public static final ConditionCode ugt = new ConditionCode("ugt");
//    public static final ConditionCode uge = new ConditionCode("uge");
//    public static final ConditionCode ult = new ConditionCode("ult");
//    public static final ConditionCode ule = new ConditionCode("ule");
//    public static final ConditionCode sgt = new ConditionCode("sgt");
//    public static final ConditionCode sge = new ConditionCode("sge");
//    public static final ConditionCode slt = new ConditionCode("slt");
//    public static final ConditionCode sle = new ConditionCode("sle");
}
