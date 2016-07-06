package polyllvm.ast;

import java.util.List;

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
import polyllvm.ast.PseudoLLVM.Expressions.LLVMDoubleLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMFloatLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMDoubleType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFloatType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVoidType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMFAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp.IConditionCode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.ast.PseudoLLVM.Statements.LLVMMul;
import polyllvm.ast.PseudoLLVM.Statements.LLVMRet;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeqLabel;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSub;

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

    LLVMBlock LLVMBlock(Position pos, List<LLVMInstruction> instructions);

    LLVMFunction LLVMFunction(Position pos, String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks);

    LLVMFunction LLVMFunction(Position compilerGenerated, String name,
            List<LLVMArgDecl> args, LLVMTypeNode retType, LLVMBlock code);

    LLVMFunctionDeclaration LLVMFunctionDeclaration(Position pos, String name,
            List<LLVMArgDecl> args, LLVMTypeNode retType);

    LLVMArgDecl LLVMArgDecl(Position pos, LLVMTypeNode typeNode, String name);

    /*
     * LLVM Expressions
     */

    LLVMIntLiteral LLVMIntLiteral(Position pos, LLVMTypeNode tn, int value);

    LLVMFloatLiteral LLVMFloatLiteral(LLVMTypeNode typeNode, float value);

    LLVMDoubleLiteral LLVMDoubleLiteral(LLVMTypeNode typeNode, double value);

    LLVMVariable LLVMVariable(Position pos, String name, LLVMTypeNode tn,
            VarType t);

    LLVMTypedOperand LLVMTypedOperand(Position pos, LLVMOperand op,
            LLVMTypeNode tn);

    LLVMLabel LLVMLabel(Position pos, String name);

    /*
     * LLVM Type Nodes
     */

    LLVMIntType LLVMIntType(Position pos, int intSize);

    LLVMDoubleType LLVMDoubleType();

    LLVMFloatType LLVMFloatType();

    LLVMVoidType LLVMVoidType(Position pos);

    LLVMPointerType LLVMPointerType(Position pos, LLVMTypeNode tn);

    LLVMFunctionType LLVMFunctionType(Position compilerGenerated,
            List<LLVMTypeNode> formalTypes, LLVMTypeNode returnType);

    /*
     * LLVM Statements (complete instructions)
     */

    LLVMAdd LLVMAdd(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right);

    LLVMAdd LLVMAdd(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMSub LLVMSub(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right);

    LLVMSub LLVMSub(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMMul LLVMMul(Position pos, LLVMVariable r, LLVMIntType t,
            LLVMOperand left, LLVMOperand right);

    LLVMMul LLVMMul(Position pos, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMFAdd LLVMFAdd(Position pos, LLVMVariable r, LLVMTypeNode tn,
            LLVMOperand left, LLVMOperand right);

    LLVMFAdd LLVMFAdd(Position pos, LLVMTypeNode tn, LLVMOperand left,
            LLVMOperand right);

    LLVMICmp LLVMICmp(Position pos, LLVMVariable result, LLVMIntType returnType,
            IConditionCode cc, LLVMIntType tn, LLVMOperand left,
            LLVMOperand right);

    LLVMICmp LLVMICmp(Position pos, LLVMIntType returnType, IConditionCode cc,
            LLVMIntType tn, LLVMOperand left, LLVMOperand right);

    LLVMBr LLVMBr(Position pos, LLVMTypedOperand cond, LLVMLabel trueLabel,
            LLVMLabel falseLabel);

    LLVMBr LLVMBr(Position pos, LLVMLabel l);

    LLVMCall LLVMCall(Position pos, LLVMVariable function,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments,
            LLVMTypeNode retType);

    LLVMRet LLVMRet(Position pos);

    LLVMRet LLVMRet(Position pos, LLVMTypeNode t, LLVMOperand o);

    LLVMAlloca LLVMAlloca(Position pos, LLVMTypeNode typeNode);

    LLVMAlloca LLVMAlloca(Position pos, LLVMTypeNode typeNode, int numElements);

    LLVMAlloca LLVMAlloca(Position pos, LLVMTypeNode typeNode, int numElements,
            int alignment);

    LLVMLoad LLVMLoad(Position pos, LLVMVariable result, LLVMTypeNode typeNode,
            LLVMVariable ptr);

    LLVMStore LLVMStore(Position pos, LLVMTypeNode typeNode, LLVMOperand value,
            LLVMVariable ptr);

    LLVMConversion LLVMConversion(Position pos, Instruction instruction,
            LLVMVariable result, LLVMTypeNode valueType, LLVMOperand value,
            LLVMTypeNode toType);

    LLVMConversion LLVMConversion(Position pos, Instruction instruction,
            LLVMTypeNode valueType, LLVMOperand value, LLVMTypeNode toType);

    /*
     * PseudoLLVM constructs
     */

    LLVMSeq LLVMSeq(Position pos, List<LLVMInstruction> instructions);

    LLVMSeqLabel LLVMSeqLabel(Position pos, String name);

    LLVMSeqLabel LLVMSeqLabel(LLVMLabel l);

    LLVMESeq LLVMESeq(Position pos, LLVMInstruction instruction,
            LLVMOperand expr);

}
