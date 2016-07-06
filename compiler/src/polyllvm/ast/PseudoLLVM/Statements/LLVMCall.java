package polyllvm.ast.PseudoLLVM.Statements;

import java.util.List;

import polyglot.util.Pair;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMCall extends LLVMInstruction {

    @Override
    LLVMCall result(LLVMVariable o);

    List<Pair<LLVMTypeNode, LLVMOperand>> arguments();

    LLVMCall arguments(List<Pair<LLVMTypeNode, LLVMOperand>> args);

}
