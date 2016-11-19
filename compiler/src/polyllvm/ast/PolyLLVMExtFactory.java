package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

/**
 * Extension factory for polyllvm extension.
 */
public interface PolyLLVMExtFactory extends ExtFactory {
    // TODO: Declare any factory methods for new extension nodes.

    Ext extLLVMNode();

    Ext extLLVMInstruction();

    Ext extLLVMBinaryOperandInstruction();

    Ext extLLVMAdd();

    Ext extLLVMIntLiteral();

    Ext extLLVMIntType();

    Ext extLLVMVariable();

    Ext extLLVMBlock();

    Ext extLLVMFunction();

    Ext extLLVMArgDecl();

    Ext extLLVMVoidType();

    Ext extLLVMRet();

    Ext extLLVMSourceFile();

    Ext extLLVMCall();

    Ext extLLVMFunctionDeclaration();

    Ext extLLVMLabel();

    Ext extLLVMExpr();

    Ext extLLVMTypedOperand();

    Ext extLLVMBr();

    Ext extLLVMOperand();

    Ext extLLVMSeq();

    Ext extLLVMSeqLabel();

    Ext extLLVMCmp();

    Ext extLLVMICmp();

    Ext extLLVMSub();

    Ext extLLVMAlloca();

    Ext extLLVMLoad();

    Ext extLLVMStore();

    Ext extLLVMMul();

    Ext extLLVMESeq();

    Ext extLLVMFunctionType();

    Ext extLLVMPointerType();

    Ext extLLVMConversion();

    Ext extLLVMFloatType();

    Ext extLLVMDoubleType();

    Ext extLLVMFloatLiteral();

    Ext extLLVMDoubleLiteral();

    Ext extLLVMFAdd();

    Ext extLLVMStructureType();

    Ext extLLVMGlobalDeclaration();

    Ext extLLVMVariableType();

    Ext extLLVMTypeDeclaration();

    Ext extLLVMNullLiteral();

    Ext extLLVMArrayLiteral();

    Ext extLLVMStructLiteral();

    Ext extLLVMGlobalVarDeclaration();

    Ext extLLVMGetElementPtr();

    Ext extLLVMArrayType();

    Ext extLLVMBitwiseBinaryInstruction();

    Ext extLLVMCStringLiteral();
}
