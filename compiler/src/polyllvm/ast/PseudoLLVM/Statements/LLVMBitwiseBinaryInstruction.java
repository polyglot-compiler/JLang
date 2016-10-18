package polyllvm.ast.PseudoLLVM.Statements;

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;

public interface LLVMBitwiseBinaryInstruction extends LLVMBinaryOperandInstruction {
    /** LLVM Conversion instruction type. */
    public static class Op extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Op(String name) {
            super(name);
        }
    }

    public static final LLVMBitwiseBinaryInstruction.Op SHL = new LLVMBitwiseBinaryInstruction.Op("shl");
    public static final LLVMBitwiseBinaryInstruction.Op LSHR = new LLVMBitwiseBinaryInstruction.Op("lshr");
    public static final LLVMBitwiseBinaryInstruction.Op ASHR = new LLVMBitwiseBinaryInstruction.Op("ashr");
    public static final LLVMBitwiseBinaryInstruction.Op AND = new LLVMBitwiseBinaryInstruction.Op("and");
    public static final LLVMBitwiseBinaryInstruction.Op OR = new LLVMBitwiseBinaryInstruction.Op("or");
    public static final LLVMBitwiseBinaryInstruction.Op XOR = new LLVMBitwiseBinaryInstruction.Op("xor");


    /**
     * Return a new LLVMBitwiseBinaryInstruction with the result variable {@code o}
     */
    @Override
    LLVMBitwiseBinaryInstruction result(LLVMVariable o);

    /**
     * Return a new LLVMBitwiseBinaryInstruction with the new result type being {@code i}
     */
    LLVMBitwiseBinaryInstruction intType(LLVMIntType i);

    /**
     * Return a new LLVMBitwiseBinaryInstruction with the new left operand being {@code l}
     */
    @Override
    LLVMBitwiseBinaryInstruction left(LLVMOperand l);

    /**
     * Return a new LLVMBitwiseBinaryInstruction with the new right operand being {@code r}
     */
    @Override
    LLVMBitwiseBinaryInstruction right(LLVMOperand r);

    /**
     * @return The int type of this instruction
     */
    LLVMIntType intType();
}
