package polyllvm.ast;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Source;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVoidType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMRet;

/**
 * NodeFactory for polyllvm extension.
 */
public interface PolyLLVMNodeFactory extends NodeFactory {

    // Factory method for Extension factory
    PolyLLVMExtFactory PolyLLVMExtFactory();

    // TODO: Declare any factory methods for new AST nodes.

    LLVMSourceFile LLVMSourceFile(Position pos, String name, Source s,
            List<LLVMFunction> funcs, List<LLVMFunctionDeclaration> funcdecls,
            List<LLVMGlobalDeclaration> globals);

    LLVMBlock LLVMBlock(Position pos, List<LLVMInstruction> instructions,
            Ext e);

    LLVMFunction LLVMFunction(Position pos, String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks);

    LLVMFunction LLVMFunction(Position compilerGenerated, String name,
            List<LLVMArgDecl> args, LLVMTypeNode retType, LLVMBlock code);

    LLVMArgDecl LLVMArgDecl(Position pos, LLVMTypeNode typeNode, String name);

    /*
     * LLVM Expressions
     */

    LLVMIntLiteral LLVMIntLiteral(Position pos, int value, Ext ext);

    LLVMVariable LLVMVariable(Position pos, String name, VarType t, Ext e);

    /*
     * LLVM Type Nodes
     */

    LLVMIntType LLVMIntType(Position pos, int intSize, Ext e);

    LLVMVoidType LLVMVoidType(Position pos);

    /*
     * LLVM Statements (complete instructions)
     */

    LLVMAdd LLVAdd(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right, Ext e);

    LLVMAdd LLVAdd(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right, Ext e);

    LLVMCall LLVMCall(Position pos, LLVMVariable function,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments,
            LLVMTypeNode retType, Ext e);

    LLVMRet LLVMRet(Position pos);

    LLVMRet LLVMRet(Position pos, LLVMTypeNode t, LLVMOperand o);

}
