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

}
