package polyllvm.ast.PseudoLLVM.Statements;

import java.util.List;

import polyglot.util.Pair;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public interface LLVMCall extends LLVMInstruction {

    @Override
    LLVMCall result(LLVMVariable o);

    List<Pair<LLVMTypeNode, LLVMOperand>> arguments();

    LLVMCall function(LLVMVariable function);

    LLVMCall arguments(List<Pair<LLVMTypeNode, LLVMOperand>> args);

    LLVMCall retType(LLVMTypeNode retType);

    LLVMFunctionDeclaration functionDeclaration(PolyLLVMNodeFactory nf);

}
