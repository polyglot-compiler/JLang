package polyllvm.ast.PseudoLLVM.Expressions;

/**
 * @author Daniel
 *
 */
public interface LLVMVariable extends LLVMOperand {

    public enum VarType {
        LOCAL, GLOBAL
    }

    public LLVMVariable name(String s);

    public String name();
}
