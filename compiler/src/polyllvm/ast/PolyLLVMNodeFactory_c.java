package polyllvm.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import polyglot.ast.NodeFactory_c;
import polyglot.frontend.Source;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl_c;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMBlock_c;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMFunctionDeclaration_c;
import polyllvm.ast.PseudoLLVM.LLVMFunction_c;
import polyllvm.ast.PseudoLLVM.LLVMGlobalDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration.GlobalVariableKind;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration_c;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile_c;
import polyllvm.ast.PseudoLLVM.LLVMTypeDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMTypeDeclaration_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMDoubleLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMDoubleLiteral_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMFloatLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMFloatLiteral_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMNullLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMNullLiteral_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMDoubleType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMDoubleType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFloatType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFloatType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVariableType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVariableType_c;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVoidType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMVoidType_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAdd_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall;
import polyllvm.ast.PseudoLLVM.Statements.LLVMCall_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion.Instruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMConversion_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMFAdd;
import polyllvm.ast.PseudoLLVM.Statements.LLVMFAdd_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMGetElementPtr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMGetElementPtr_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp.IConditionCode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMMul;
import polyllvm.ast.PseudoLLVM.Statements.LLVMMul_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMRet;
import polyllvm.ast.PseudoLLVM.Statements.LLVMRet_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeqLabel;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeqLabel_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore_c;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSub;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSub_c;

/**
 * NodeFactory for polyllvm extension.
 */
public class PolyLLVMNodeFactory_c extends NodeFactory_c
        implements PolyLLVMNodeFactory {
    public PolyLLVMNodeFactory_c(PolyLLVMLang lang,
            PolyLLVMExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public PolyLLVMExtFactory extFactory() {
        return (PolyLLVMExtFactory) super.extFactory();
    }

    @Override
    public PolyLLVMExtFactory PolyLLVMExtFactory() {
        return new PolyLLVMExtFactory_c();
    }

    // TODO:  Implement factory methods for new AST nodes.

    @Override
    public LLVMAdd LLVMAdd(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right) {
        LLVMAdd n = new LLVMAdd_c(Position.compilerGenerated(),
                                  r,
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMAdd());
    }

    @Override
    public LLVMAdd LLVMAdd(LLVMIntType t, LLVMOperand left, LLVMOperand right) {
        LLVMAdd n = new LLVMAdd_c(Position.compilerGenerated(),
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMAdd());
    }

    @Override
    public LLVMSub LLVMSub(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right) {
        LLVMSub n = new LLVMSub_c(Position.compilerGenerated(),
                                  r,
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMSub());
    }

    @Override
    public LLVMSub LLVMSub(LLVMIntType t, LLVMOperand left, LLVMOperand right) {
        LLVMSub n = new LLVMSub_c(Position.compilerGenerated(),
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMSub());
    }

    @Override
    public LLVMMul LLVMMul(LLVMVariable r, LLVMIntType t, LLVMOperand left,
            LLVMOperand right) {
        LLVMMul n = new LLVMMul_c(Position.compilerGenerated(),
                                  r,
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMMul());
    }

    @Override
    public LLVMMul LLVMMul(LLVMIntType t, LLVMOperand left, LLVMOperand right) {
        LLVMMul n = new LLVMMul_c(Position.compilerGenerated(),
                                  t,
                                  left,
                                  right,
                                  null);
        return ext(n, extFactory().extLLVMMul());
    }

    @Override
    public LLVMIntLiteral LLVMIntLiteral(LLVMTypeNode tn, long value) {
        LLVMIntLiteral n = new LLVMIntLiteral_c(Position.compilerGenerated(),
                                                value,
                                                tn,
                                                null);
        return ext(n, extFactory().extLLVMIntLiteral());
    }

    @Override
    public LLVMIntType LLVMIntType(int intSize) {
        LLVMIntType n =
                new LLVMIntType_c(Position.compilerGenerated(), intSize, null);
        return ext(n, extFactory().extLLVMIntType());
    }

    @Override
    public LLVMVariable LLVMVariable(String name, LLVMTypeNode tn, VarType t) {
        LLVMVariable n = new LLVMVariable_c(Position.compilerGenerated(),
                                            name,
                                            tn,
                                            t,
                                            null);
        return ext(n, extFactory().extLLVMVariable());
    }

    @Override
    public LLVMBlock LLVMBlock(List<LLVMInstruction> instructions) {
        LLVMBlock n = new LLVMBlock_c(Position.compilerGenerated(),
                                      instructions,
                                      null);
        return ext(n, extFactory().extLLVMBlock());
    }

    @Override
    public LLVMFunction LLVMFunction(String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, List<LLVMBlock> blocks) {
        LLVMFunction n = new LLVMFunction_c(Position.compilerGenerated(),
                                            name,
                                            args,
                                            retType,
                                            blocks);
        return ext(n, extFactory().extLLVMFunction());
    }

    @Override
    public LLVMFunction LLVMFunction(String name, List<LLVMArgDecl> args,
            LLVMTypeNode retType, LLVMBlock code) {
        List<LLVMBlock> blocks = new ArrayList<>(Arrays.asList(code));
        return LLVMFunction(name, args, retType, blocks);
    }

    @Override
    public LLVMArgDecl LLVMArgDecl(LLVMTypeNode typeNode, String name) {
        LLVMArgDecl n =
                new LLVMArgDecl_c(Position.compilerGenerated(), typeNode, name);
        return ext(n, extFactory().extLLVMArgDecl());
    }

    @Override
    public LLVMVoidType LLVMVoidType() {
        LLVMVoidType n = new LLVMVoidType_c(Position.compilerGenerated());
        return ext(n, extFactory().extLLVMVoidType());
    }

    @Override
    public LLVMRet LLVMRet() {
        LLVMRet n = new LLVMRet_c(Position.compilerGenerated());
        return ext(n, extFactory().extLLVMRet());
    }

    @Override
    public LLVMRet LLVMRet(LLVMTypeNode t, LLVMOperand o) {
        LLVMRet n = new LLVMRet_c(Position.compilerGenerated(), t, o);
        return ext(n, extFactory().extLLVMRet());
    }

    @Override
    public LLVMSourceFile LLVMSourceFile(String name, Source s,
            List<LLVMFunction> funcs, List<LLVMFunctionDeclaration> funcdecls,
            List<LLVMGlobalDeclaration> globals) {
        LLVMSourceFile n =
                new LLVMSourceFile_c(Position.compilerGenerated(),
                                     name,
                                     s,
                                     funcs,
                                     funcdecls,
                                     globals);
        return ext(n, extFactory().extLLVMSourceFile());
    }

    @Override
    public LLVMCall LLVMCall(LLVMVariable function,
            List<Pair<LLVMTypeNode, LLVMOperand>> arguments,
            LLVMTypeNode retType) {
        LLVMCall n = new LLVMCall_c(Position.compilerGenerated(),
                                    function,
                                    arguments,
                                    retType,
                                    null);
        return ext(n, extFactory().extLLVMCall());
    }

    @Override
    public LLVMFunctionDeclaration LLVMFunctionDeclaration(String name,
            List<LLVMArgDecl> args, LLVMTypeNode retType) {
        LLVMFunctionDeclaration n =
                new LLVMFunctionDeclaration_c(Position.compilerGenerated(),
                                              name,
                                              args,
                                              retType,
                                              null);
        return ext(n, extFactory().extLLVMFunctionDeclaration());
    }

    @Override
    public LLVMLabel LLVMLabel(String name) {
        LLVMLabel n = new LLVMLabel_c(Position.compilerGenerated(), name, null);
        return ext(n, extFactory().extLLVMLabel());
    }

    @Override
    public LLVMTypedOperand LLVMTypedOperand(LLVMOperand op, LLVMTypeNode tn) {
        LLVMTypedOperand n =
                new LLVMTypedOperand_c(Position.compilerGenerated(),
                                       op,
                                       tn,
                                       null);//(Position.compilerGenerated(), name, t, tn, null);
        return ext(n, extFactory().extLLVMTypedOperand());
    }

    @Override
    public LLVMBr LLVMBr(LLVMTypedOperand cond, LLVMLabel trueLabel,
            LLVMLabel falseLabel) {
        LLVMBr n = new LLVMBr_c(Position.compilerGenerated(),
                                cond,
                                trueLabel,
                                falseLabel,
                                null);
        return ext(n, extFactory().extLLVMBr());
    }

    @Override
    public LLVMBr LLVMBr(LLVMLabel l) {
        LLVMBr n = new LLVMBr_c(Position.compilerGenerated(), l, null);
        return ext(n, extFactory().extLLVMBr());
    }

    @Override
    public LLVMSeq LLVMSeq(List<LLVMInstruction> instructions) {
        LLVMSeq n =
                new LLVMSeq_c(Position.compilerGenerated(), instructions, null);
        return ext(n, extFactory().extLLVMSeq());
    }

    @Override
    public LLVMSeqLabel LLVMSeqLabel(String name) {
        LLVMSeqLabel n =
                new LLVMSeqLabel_c(Position.compilerGenerated(), name, null);
        return ext(n, extFactory().extLLVMSeqLabel());
    }

    @Override
    public LLVMSeqLabel LLVMSeqLabel(LLVMLabel l) {
        LLVMSeqLabel n = new LLVMSeqLabel_c(Position.compilerGenerated(),
                                            l.name(),
                                            null);
        return ext(n, extFactory().extLLVMSeqLabel());
    }

    @Override
    public LLVMICmp LLVMICmp(LLVMVariable result, LLVMIntType returnType,
            IConditionCode cc, LLVMIntType tn, LLVMOperand left,
            LLVMOperand right) {
        LLVMICmp n =
                new LLVMICmp_c(Position.compilerGenerated(),
                               result,
                               returnType,
                               cc,
                               tn,
                               left,
                               right,
                               null);
        return ext(n, extFactory().extLLVMICmp());
    }

    @Override
    public polyllvm.ast.PseudoLLVM.Statements.LLVMICmp LLVMICmp(
            LLVMIntType returnType, IConditionCode cc,
            polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType tn, LLVMOperand left,
            LLVMOperand right) {
        LLVMICmp n = new LLVMICmp_c(Position.compilerGenerated(),
                                    returnType,
                                    cc,
                                    tn,
                                    left,
                                    right,
                                    null);
        return ext(n, extFactory().extLLVMICmp());
    }

    @Override
    public LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode) {
        LLVMAlloca n =
                new LLVMAlloca_c(Position.compilerGenerated(), typeNode, null);
        return ext(n, extFactory().extLLVMAlloca());
    }

    @Override
    public LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode, int numElements) {
        LLVMAlloca n = new LLVMAlloca_c(Position.compilerGenerated(),
                                        typeNode,
                                        numElements,
                                        null);
        return ext(n, extFactory().extLLVMAlloca());
    }

    @Override
    public LLVMAlloca LLVMAlloca(LLVMTypeNode typeNode, int numElements,
            int alignment) {
        LLVMAlloca n = new LLVMAlloca_c(Position.compilerGenerated(),
                                        typeNode,
                                        numElements,
                                        alignment,
                                        null);
        return ext(n, extFactory().extLLVMAlloca());
    }

    @Override
    public LLVMLoad LLVMLoad(LLVMVariable var, LLVMTypeNode typeNode,
            LLVMOperand ptr) {
        LLVMLoad n = new LLVMLoad_c(Position.compilerGenerated(),
                                    var,
                                    typeNode,
                                    ptr,
                                    null);
        return ext(n, extFactory().extLLVMLoad());
    }

    @Override
    public LLVMStore LLVMStore(LLVMTypeNode typeNode, LLVMOperand value,
            LLVMOperand ptr) {
        LLVMStore n = new LLVMStore_c(Position.compilerGenerated(),
                                      typeNode,
                                      value,
                                      ptr,
                                      null);
        return ext(n, extFactory().extLLVMStore());
    }

    @Override
    public LLVMESeq LLVMESeq(LLVMInstruction instruction, LLVMOperand expr) {
        LLVMESeq n = new LLVMESeq_c(Position.compilerGenerated(),
                                    instruction,
                                    expr,
                                    null);
        return ext(n, extFactory().extLLVMESeq());
    }

    @Override
    public LLVMFunctionType LLVMFunctionType(List<LLVMTypeNode> formalTypes,
            LLVMTypeNode returnType) {
        LLVMFunctionType n =
                new LLVMFunctionType_c(Position.compilerGenerated(),
                                       formalTypes,
                                       returnType,
                                       null);
        return ext(n, extFactory().extLLVMFunctionType());
    }

    @Override
    public LLVMPointerType LLVMPointerType(LLVMTypeNode tn) {
        LLVMPointerType n =
                new LLVMPointerType_c(Position.compilerGenerated(), tn, null);
        return ext(n, extFactory().extLLVMPointerType());
    }

    @Override
    public LLVMConversion LLVMConversion(Instruction instruction,
            LLVMVariable result, LLVMTypeNode valueType, LLVMOperand value,
            LLVMTypeNode toType) {
        LLVMConversion n =
                new LLVMConversion_c(Position.compilerGenerated(),
                                     instruction,
                                     result,
                                     valueType,
                                     value,
                                     toType,
                                     null);
        return ext(n, extFactory().extLLVMConversion());
    }

    @Override
    public polyllvm.ast.PseudoLLVM.Statements.LLVMConversion LLVMConversion(
            Instruction instruction, LLVMTypeNode valueType, LLVMOperand value,
            LLVMTypeNode toType) {
        LLVMConversion n = new LLVMConversion_c(Position.compilerGenerated(),
                                                instruction,
                                                valueType,
                                                value,
                                                toType,
                                                null);
        return ext(n, extFactory().extLLVMConversion());
    }

    @Override
    public LLVMDoubleType LLVMDoubleType() {
        LLVMDoubleType n =
                new LLVMDoubleType_c(Position.compilerGenerated(), null);
        return ext(n, extFactory().extLLVMDoubleType());
    }

    @Override
    public LLVMFloatType LLVMFloatType() {
        LLVMFloatType n =
                new LLVMFloatType_c(Position.compilerGenerated(), null);
        return ext(n, extFactory().extLLVMFloatType());
    }

    @Override
    public LLVMFloatLiteral LLVMFloatLiteral(LLVMTypeNode typeNode,
            float value) {
        LLVMFloatLiteral n =
                new LLVMFloatLiteral_c(Position.compilerGenerated(),
                                       typeNode,
                                       value,
                                       null);
        return ext(n, extFactory().extLLVMFloatLiteral());
    }

    @Override
    public LLVMDoubleLiteral LLVMDoubleLiteral(LLVMTypeNode typeNode,
            double value) {
        LLVMDoubleLiteral n =
                new LLVMDoubleLiteral_c(Position.compilerGenerated(),
                                        typeNode,
                                        value,
                                        null);
        return ext(n, extFactory().extLLVMDoubleLiteral());
    }

    @Override
    public LLVMFAdd LLVMFAdd(LLVMVariable result, LLVMTypeNode tn,
            LLVMOperand left, LLVMOperand right) {
        LLVMFAdd n = new LLVMFAdd_c(Position.compilerGenerated(),
                                    result,
                                    tn,
                                    left,
                                    right,
                                    null);
        return ext(n, extFactory().extLLVMFAdd());
    }

    @Override
    public LLVMFAdd LLVMFAdd(LLVMTypeNode tn, LLVMOperand left,
            LLVMOperand right) {
        LLVMFAdd n = new LLVMFAdd_c(Position.compilerGenerated(),
                                    tn,
                                    left,
                                    right,
                                    null);
        return ext(n, extFactory().extLLVMFAdd());
    }

    @Override
    public LLVMStructureType LLVMStructureType(List<LLVMTypeNode> typeList) {
        LLVMStructureType n =
                new LLVMStructureType_c(Position.compilerGenerated(),
                                        typeList,
                                        null);
        return ext(n, extFactory().extLLVMStructureType());
    }

    @Override
    public LLVMVariableType LLVMVariableType(String name) {
        LLVMVariableType n =
                new LLVMVariableType_c(Position.compilerGenerated(),
                                       name,
                                       null);
        return ext(n, extFactory().extLLVMVariableType());
    }

    @Override
    public LLVMTypeDeclaration LLVMTypeDeclaration(String typeName,
            LLVMTypeNode tn) {
        LLVMTypeDeclaration n =
                new LLVMTypeDeclaration_c(Position.compilerGenerated(),
                                          typeName,
                                          tn,
                                          null);
        return ext(n, extFactory().extLLVMTypeDeclaration());
    }

    @Override
    public LLVMNullLiteral LLVMNullLiteral(LLVMTypeNode typeNode) {
        LLVMNullLiteral n = new LLVMNullLiteral_c(Position.compilerGenerated(),
                                                  typeNode,
                                                  null);
        return ext(n, extFactory().extLLVMNullLiteral());
    }

    @Override
    public LLVMGlobalVarDeclaration LLVMGlobalVarDeclaration(String name,
            boolean isExtern, GlobalVariableKind kind, LLVMTypeNode typeNode,
            LLVMOperand initializerConstant) {
        LLVMGlobalVarDeclaration n =
                new LLVMGlobalVarDeclaration_c(Position.compilerGenerated(),
                                               name,
                                               isExtern,
                                               kind,
                                               typeNode,
                                               initializerConstant,
                                               null);
        return ext(n, extFactory().extLLVMGlobalVarDeclaration());
    }

    @Override
    public LLVMGetElementPtr LLVMGetElementPtr(LLVMOperand ptrVar,
            List<LLVMTypedOperand> l) {
        LLVMGetElementPtr n =
                new LLVMGetElementPtr_c(Position.compilerGenerated(),
                                        ptrVar,
                                        l,
                                        null);
        return ext(n, extFactory().extLLVMGetElementPtr());
    }

    @Override
    public LLVMArrayType LLVMArrayType(LLVMTypeNode arrayType, int length) {
        LLVMArrayType n = new LLVMArrayType_c(Position.compilerGenerated(),
                                              arrayType,
                                              length,
                                              null);
        return ext(n, extFactory().extLLVMArrayType());
    }

    // TODO:  Override factory methods for overridden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.

}
