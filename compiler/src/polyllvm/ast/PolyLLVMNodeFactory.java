package polyllvm.ast;

import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Source;
import polyglot.util.Pair;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration.GlobalVariableKind;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.ast.PseudoLLVM.LLVMTypeDeclaration;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMDoubleLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMFloatLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMNullLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMDoubleType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFloatType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVariableType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVoidType;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMFAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMGetElementPtr;
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

    LLVMSourceFile LLVMSourceFile(String name, Source s,
            List<LLVMFunction> funcs, List<LLVMFunctionDeclaration> funcdecls,
            List<LLVMGlobalDeclaration> globals);

    LLVMBlock LLVMBlock(List<LLVMInstruction> instructions);

    LLVMFunction LLVMFunction(String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks);

    LLVMFunction LLVMFunction(String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, LLVMBlock code);

    LLVMFunctionDeclaration LLVMFunctionDeclaration(String name,
            List<LLVMArgDecl> args, LLVMTypeNode retType);

    LLVMArgDecl LLVMArgDecl(LLVMTypeNode typeNode, String name);

    LLVMTypeDeclaration LLVMTypeDeclaration(String typeName, LLVMTypeNode tn);

    LLVMGlobalVarDeclaration LLVMGlobalVarDeclaration(String name,
            boolean isExtern, GlobalVariableKind kind, LLVMTypeNode typeNode,
            LLVMOperand initializerConstant);

    /*
     * LLVM Expressions
     */

    LLVMIntLiteral LLVMIntLiteral(LLVMTypeNode tn, long value);

    LLVMFloatLiteral LLVMFloatLiteral(LLVMTypeNode typeNode, float value);

    LLVMDoubleLiteral LLVMDoubleLiteral(LLVMTypeNode typeNode, double value);

    LLVMNullLiteral LLVMNullLiteral(LLVMTypeNode typeNode);

    LLVMVariable LLVMVariable(String name, LLVMTypeNode tn, VarType t);

    LLVMTypedOperand LLVMTypedOperand(LLVMOperand op, LLVMTypeNode tn);

    LLVMLabel LLVMLabel(String name);

    /*
     * LLVM Type Nodes
     */

    LLVMIntType LLVMIntType(int intSize);

    LLVMDoubleType LLVMDoubleType();

    LLVMFloatType LLVMFloatType();

    LLVMVariableType LLVMVariableType(String name);

    LLVMStructureType LLVMStructureType(List<LLVMTypeNode> typeList);

    LLVMArrayType LLVMArrayType(LLVMTypeNode arrayType, int length);

    LLVMVoidType LLVMVoidType();

    LLVMPointerType LLVMPointerType(LLVMTypeNode tn);

    LLVMFunctionType LLVMFunctionType(List<LLVMTypeNode> formalTypes,
            LLVMTypeNode returnType);

    /*
     * LLVM Statements (complete instructions)
     */

    LLVMAdd LLVMAdd(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMAdd LLVMAdd(LLVMIntType t, LLVMOperand left, LLVMOperand right);

    LLVMSub LLVMSub(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMSub LLVMSub(LLVMIntType t, LLVMOperand left, LLVMOperand right);

    LLVMMul LLVMMul(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right);

    LLVMMul LLVMMul(LLVMIntType t, LLVMOperand left, LLVMOperand right);

    LLVMFAdd LLVMFAdd(LLVMVariable r, LLVMTypeNode tn, LLVMOperand left,
            LLVMOperand right);

    LLVMFAdd LLVMFAdd(LLVMTypeNode tn, LLVMOperand left, LLVMOperand right);

    LLVMICmp LLVMICmp(LLVMVariable result, LLVMIntType returnType,
            IConditionCode cc, LLVMIntType tn, LLVMOperand left,
            LLVMOperand right);

    LLVMICmp LLVMICmp(LLVMIntType returnType, IConditionCode cc, LLVMIntType tn,
            LLVMOperand left, LLVMOperand right);

    LLVMBr LLVMBr(LLVMTypedOperand cond, LLVMLabel trueLabel,
            LLVMLabel falseLabel);

    LLVMBr LLVMBr(LLVMLabel l);

    LLVMCall LLVMCall(LLVMVariable function,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments,
            LLVMTypeNode retType);

    LLVMRet LLVMRet();

    LLVMRet LLVMRet(LLVMTypeNode t, LLVMOperand o);

    LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode);

    LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode, int numElements);

    LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode, int numElements,
            int alignment);

    LLVMLoad LLVMLoad(LLVMVariable result, LLVMTypeNode typeNode,
            LLVMOperand ptr);

    LLVMStore LLVMStore(LLVMTypeNode typeNode, LLVMOperand value,
            LLVMOperand ptr);

    LLVMConversion LLVMConversion(Instruction instruction, LLVMVariable result,
            LLVMTypeNode valueType, LLVMOperand value, LLVMTypeNode toType);

    LLVMConversion LLVMConversion(Instruction instruction,
            LLVMTypeNode valueType, LLVMOperand value, LLVMTypeNode toType);

    LLVMGetElementPtr LLVMGetElementPtr(LLVMOperand thisTranslation,
            List<LLVMTypedOperand> l);

    /*
     * PseudoLLVM constructs
     */

    LLVMSeq LLVMSeq(List<LLVMInstruction> instructions);

    LLVMSeqLabel LLVMSeqLabel(String name);

    LLVMSeqLabel LLVMSeqLabel(LLVMLabel l);

    LLVMESeq LLVMESeq(LLVMInstruction instruction, LLVMOperand expr);

}
