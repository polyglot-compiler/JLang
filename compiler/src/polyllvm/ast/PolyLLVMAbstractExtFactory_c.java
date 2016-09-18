package polyllvm.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyllvm.extension.PseudoLLVM.PolyLLVMArgDeclExt;
import polyllvm.extension.PseudoLLVM.PolyLLVMBinaryOperandInstructionExt;
import polyllvm.extension.PseudoLLVM.PolyLLVMBrExt;
import polyllvm.extension.PseudoLLVM.PolyLLVMCallExt;
import polyllvm.extension.PseudoLLVM.PolyLLVMFunctionExt;

public abstract class PolyLLVMAbstractExtFactory_c extends AbstractExtFactory_c
        implements PolyLLVMExtFactory {

    public PolyLLVMAbstractExtFactory_c() {
        super();
    }

    public PolyLLVMAbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    // TODO: Implement factory methods for new extension nodes in future
    // extensions.  This entails calling the factory method for extension's
    // AST superclass.

    @Override
    public final Ext extLLVMNode() {
        Ext e = extLLVMNodeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMNode();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMNode(e);
    }

    protected Ext extLLVMNodeImpl() {
        return extNodeImpl();
    }

    protected Ext postExtLLVMNode(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMInstruction() {
        Ext e = extLLVMInstructionImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMInstruction();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMInstruction(e);
    }

    protected Ext extLLVMInstructionImpl() {
        return extNodeImpl();
    }

    protected Ext postExtLLVMInstruction(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMBinaryOperandInstruction() {
        Ext e = extLLVMBinaryOperandInstructionImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMBinaryOperandInstruction();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMBinaryOperandInstruction(e);
    }

    protected Ext extLLVMBinaryOperandInstructionImpl() {
        return new PolyLLVMBinaryOperandInstructionExt();
    }

    protected Ext postExtLLVMBinaryOperandInstruction(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public final Ext extLLVMAdd() {
        Ext e = extLLVMAddImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMAdd();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMAdd(e);
    }

    protected Ext extLLVMAddImpl() {
        return extLLVMBinaryOperandInstructionImpl();
    }

    protected Ext postExtLLVMAdd(Ext e) {
        return postExtLLVMBinaryOperandInstruction(e);
    }

    @Override
    public final Ext extLLVMSub() {
        Ext e = extLLVMSubImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMSub();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMSub(e);
    }

    protected Ext extLLVMSubImpl() {
        return extLLVMBinaryOperandInstructionImpl();
    }

    protected Ext postExtLLVMSub(Ext e) {
        return postExtLLVMBinaryOperandInstruction(e);
    }

    @Override
    public final Ext extLLVMMul() {
        Ext e = extLLVMMulImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMMul();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMMul(e);
    }

    protected Ext extLLVMMulImpl() {
        return extLLVMBinaryOperandInstructionImpl();
    }

    protected Ext postExtLLVMMul(Ext e) {
        return postExtLLVMBinaryOperandInstruction(e);
    }

    @Override
    public final Ext extLLVMIntLiteral() {
        Ext e = extLLVMIntLiteralImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMIntLiteral();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMIntLiteral(e);
    }

    protected Ext extLLVMIntLiteralImpl() {
        return extNode();
    }

    protected Ext postExtLLVMIntLiteral(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMIntType() {
        Ext e = extLLVMIntTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMIntType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMIntType(e);
    }

    protected Ext extLLVMIntTypeImpl() {
        return extNode();
    }

    protected Ext postExtLLVMIntType(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMFloatLiteral() {
        Ext e = extLLVMFloatLiteralImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFloatLiteral();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFloatLiteral(e);
    }

    protected Ext extLLVMFloatLiteralImpl() {
        return extNode();
    }

    protected Ext postExtLLVMFloatLiteral(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMFloatType() {
        Ext e = extLLVMFloatTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFloatType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFloatType(e);
    }

    protected Ext extLLVMFloatTypeImpl() {
        return extNode();
    }

    protected Ext postExtLLVMFloatType(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMDoubleLiteral() {
        Ext e = extLLVMDoubleLiteralImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMDoubleLiteral();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMDoubleLiteral(e);
    }

    protected Ext extLLVMDoubleLiteralImpl() {
        return extNode();
    }

    protected Ext postExtLLVMDoubleLiteral(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMDoubleType() {
        Ext e = extLLVMDoubleTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMDoubleType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMDoubleType(e);
    }

    protected Ext extLLVMDoubleTypeImpl() {
        return extNode();
    }

    protected Ext postExtLLVMDoubleType(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMFunctionType() {
        Ext e = extLLVMFunctionTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFunctionType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFunctionType(e);
    }

    protected Ext extLLVMFunctionTypeImpl() {
        return extNode(); //TODO : implement extension for LLVMTypeNode
    }

    protected Ext postExtLLVMFunctionType(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMPointerType() {
        Ext e = extLLVMPointerTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMPointerType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMPointerType(e);
    }

    protected Ext extLLVMPointerTypeImpl() {
        return extNode(); //TODO : implement extension for LLVMTypeNode
    }

    protected Ext postExtLLVMPointerType(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMVariable() {
        Ext e = extLLVMVariableImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMVariable();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMVariable(e);
    }

    protected Ext extLLVMVariableImpl() {
        return extNode();
    }

    protected Ext postExtLLVMVariable(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMBlock() {
        Ext e = extLLVMBlockImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMBlock();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMBlock(e);
    }

    protected Ext extLLVMBlockImpl() {
        return extNode();
    }

    protected Ext postExtLLVMBlock(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMFunction() {
        Ext e = extLLVMFunctionImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFunction();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFunction(e);
    }

    protected Ext extLLVMFunctionImpl() {
        return new PolyLLVMFunctionExt();
    }

    protected Ext postExtLLVMFunction(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public final Ext extLLVMArgDecl() {
        Ext e = extLLVMArgDeclImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFunction();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMArgDecl(e);
    }

    protected Ext extLLVMArgDeclImpl() {
        return new PolyLLVMArgDeclExt();
    }

    protected Ext postExtLLVMArgDecl(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public final Ext extLLVMVoidType() {
        Ext e = extLLVMVoidTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMVoidType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMVoidType(e);
    }

    protected Ext extLLVMVoidTypeImpl() {
        return extLLVMNode();
    }

    protected Ext postExtLLVMVoidType(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public final Ext extLLVMRet() {
        Ext e = extLLVMRetImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMRet();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMRet(e);
    }

    protected Ext extLLVMRetImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMRet(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public final Ext extLLVMSourceFile() {
        Ext e = extLLVMSourceFileImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMSourceFile();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMSourceFile(e);
    }

    protected Ext extLLVMSourceFileImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMSourceFile(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public final Ext extLLVMCall() {
        Ext e = extLLVMCallImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMCall();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMCall(e);
    }

    protected Ext extLLVMCallImpl() {
        return new PolyLLVMCallExt();
    }

    protected Ext postExtLLVMCall(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMFunctionDeclaration() {
        Ext e = extLLVMFunctionDeclarationImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFunctionDeclaration();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFunctionDeclaration(e);

    }

    protected Ext extLLVMFunctionDeclarationImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMFunctionDeclaration(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public Ext extLLVMLabel() {
        Ext e = extLLVMLabelImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMLabel();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMLabel(e);

    }

    protected Ext extLLVMLabelImpl() {
        return extLLVMExprImpl();
    }

    protected Ext postExtLLVMLabel(Ext e) {
        return postExtLLVMExpr(e);
    }

    @Override
    public Ext extLLVMExpr() {
        Ext e = extLLVMExprImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMExpr();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMExpr(e);

    }

    protected Ext extLLVMExprImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMExpr(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public Ext extLLVMTypedOperand() {
        Ext e = extLLVMTypedOperandImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMTypedOperand();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMTypedOperand(e);

    }

    protected Ext extLLVMTypedOperandImpl() {
        return extLLVMOperandImpl();
    }

    protected Ext postExtLLVMTypedOperand(Ext e) {
        return postExtLLVMOperand(e);
    }

    @Override
    public Ext extLLVMOperand() {
        Ext e = extLLVMOperandImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMOperand();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMOperand(e);

    }

    protected Ext extLLVMOperandImpl() {
        return extLLVMExprImpl();
    }

    protected Ext postExtLLVMOperand(Ext e) {
        return postExtLLVMExpr(e);
    }

    @Override
    public Ext extLLVMBr() {
        Ext e = extLLVMBrImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMBr();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMBr(e);

    }

    protected Ext extLLVMBrImpl() {
        return new PolyLLVMBrExt();
    }

    protected Ext postExtLLVMBr(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMSeq() {
        Ext e = extLLVMSeqImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMSeq();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMSeq(e);

    }

    protected Ext extLLVMSeqImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMSeq(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMSeqLabel() {
        Ext e = extLLVMSeqLabelImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMSeqLabel();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMSeqLabel(e);

    }

    protected Ext extLLVMSeqLabelImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMSeqLabel(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMCmp() {
        Ext e = extLLVMCmpImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMCmp();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMCmp(e);

    }

    protected Ext extLLVMCmpImpl() {
        return extLLVMBinaryOperandInstructionImpl();
    }

    protected Ext postExtLLVMCmp(Ext e) {
        return postExtLLVMBinaryOperandInstruction(e);
    }

    @Override
    public Ext extLLVMICmp() {
        Ext e = extLLVMICmpImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMICmp();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMICmp(e);

    }

    protected Ext extLLVMICmpImpl() {
        return extLLVMCmpImpl();
    }

    protected Ext postExtLLVMICmp(Ext e) {
        return postExtLLVMCmp(e);
    }

    @Override
    public Ext extLLVMAlloca() {
        Ext e = extLLVMAllocaImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMAlloca();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMAlloca(e);

    }

    protected Ext extLLVMAllocaImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMAlloca(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMLoad() {
        Ext e = extLLVMLoadImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMLoad();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMLoad(e);

    }

    protected Ext extLLVMLoadImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMLoad(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMStore() {
        Ext e = extLLVMStoreImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMStore();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMStore(e);

    }

    protected Ext extLLVMStoreImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMStore(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMESeq() {
        Ext e = extLLVMESeqImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMESeq();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMESeq(e);

    }

    protected Ext extLLVMESeqImpl() {
        return extLLVMOperandImpl();
    }

    protected Ext postExtLLVMESeq(Ext e) {
        return postExtLLVMOperand(e);
    }

    @Override
    public Ext extLLVMConversion() {
        Ext e = extLLVMConversionImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMConversion();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMConversion(e);

    }

    protected Ext extLLVMConversionImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMConversion(Ext e) {
        return postExtLLVMInstruction(e);
    }

    @Override
    public Ext extLLVMFAdd() {
        Ext e = extLLVMFAddImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMFAdd();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMFAdd(e);

    }

    protected Ext extLLVMFAddImpl() {
        return extLLVMBinaryOperandInstructionImpl();
    }

    protected Ext postExtLLVMFAdd(Ext e) {
        return postExtLLVMBinaryOperandInstruction(e);
    }

    @Override
    public Ext extLLVMStructureType() {
        Ext e = extLLVMStructureTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMStructureType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMStructureType(e);

    }

    protected Ext extLLVMStructureTypeImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMStructureType(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public Ext extLLVMArrayType() {
        Ext e = extLLVMArrayTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMArrayType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMArrayType(e);

    }

    protected Ext extLLVMArrayTypeImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMArrayType(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public Ext extLLVMGlobalDeclaration() {
        Ext e = extLLVMGlobalDeclarationImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMGlobalDeclaration();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMGlobalDeclaration(e);

    }

    protected Ext extLLVMGlobalDeclarationImpl() {
        return extLLVMNodeImpl();
    }

    protected Ext postExtLLVMGlobalDeclaration(Ext e) {
        return postExtLLVMNode(e);
    }

    @Override
    public Ext extLLVMVariableType() {
        Ext e = extLLVMVariableTypeImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMVariableType();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMVariableType(e);

    }

    protected Ext extLLVMVariableTypeImpl() {
        return extLLVMNodeImpl(); //TODO : implement extension for LLVMTypeNode
    }

    protected Ext postExtLLVMVariableType(Ext e) {
        return postExtLLVMNode(e); //TODO : implement extension for LLVMTypeNode
    }

    @Override
    public Ext extLLVMTypeDeclaration() {
        Ext e = extLLVMTypeDeclarationImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMTypeDeclaration();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMTypeDeclaration(e);

    }

    protected Ext extLLVMTypeDeclarationImpl() {
        return extLLVMGlobalDeclarationImpl();
    }

    protected Ext postExtLLVMTypeDeclaration(Ext e) {
        return postExtLLVMGlobalDeclaration(e);
    }

    @Override
    public Ext extLLVMGlobalVarDeclaration() {
        Ext e = extLLVMGlobalVarDeclarationImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMGlobalVarDeclaration();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMGlobalVarDeclaration(e);

    }

    protected Ext extLLVMGlobalVarDeclarationImpl() {
        return extLLVMGlobalDeclarationImpl();
    }

    protected Ext postExtLLVMGlobalVarDeclaration(Ext e) {
        return postExtLLVMGlobalDeclaration(e);
    }

    @Override
    public final Ext extLLVMNullLiteral() {
        Ext e = extLLVMNullLiteralImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMNullLiteral();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMNullLiteral(e);
    }

    protected Ext extLLVMNullLiteralImpl() {
        return extNode();
    }

    protected Ext postExtLLVMNullLiteral(Ext e) {
        return postExtNode(e);
    }

    @Override
    public final Ext extLLVMGetElementPtr() {
        Ext e = extLLVMGetElementPtrImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extLLVMGetElementPtr();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtLLVMGetElementPtr(e);
    }

    protected Ext extLLVMGetElementPtrImpl() {
        return extLLVMInstructionImpl();
    }

    protected Ext postExtLLVMGetElementPtr(Ext e) {
        return postExtLLVMInstruction(e);
    }

}
