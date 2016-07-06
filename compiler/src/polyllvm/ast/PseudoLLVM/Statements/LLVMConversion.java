package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

public interface LLVMConversion extends LLVMInstruction {
    /** LLVM Conversion instruction type. */
    public static class Instruction extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Instruction(String name) {
            super(name);
        }
    }

    public static final Instruction SITOFP = new Instruction("sitofp");
    public static final Instruction FPEXT = new Instruction("fpext");
    public static final Instruction ZEXT = new Instruction("zext");
    public static final Instruction SEXT = new Instruction("sext");

}
