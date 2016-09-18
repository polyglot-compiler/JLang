package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyllvm.extension.PolyLLVMArrayAccessAssignExt;
import polyllvm.extension.PolyLLVMArrayAccessExt;
import polyllvm.extension.PolyLLVMArrayInitExt;
import polyllvm.extension.PolyLLVMAssignExt;
import polyllvm.extension.PolyLLVMBinaryExt;
import polyllvm.extension.PolyLLVMBlockExt;
import polyllvm.extension.PolyLLVMBooleanLitExt;
import polyllvm.extension.PolyLLVMBranchExt;
import polyllvm.extension.PolyLLVMCallExt;
import polyllvm.extension.PolyLLVMCanonicalTypeNodeExt;
import polyllvm.extension.PolyLLVMCastExt;
import polyllvm.extension.PolyLLVMCharLitExt;
import polyllvm.extension.PolyLLVMClassBodyExt;
import polyllvm.extension.PolyLLVMClassDeclExt;
import polyllvm.extension.PolyLLVMConstructorCallExt;
import polyllvm.extension.PolyLLVMConstructorDeclExt;
import polyllvm.extension.PolyLLVMEmptyExt;
import polyllvm.extension.PolyLLVMEvalExt;
import polyllvm.extension.PolyLLVMFieldAssignExt;
import polyllvm.extension.PolyLLVMFieldExt;
import polyllvm.extension.PolyLLVMFloatLitExt;
import polyllvm.extension.PolyLLVMFormalExt;
import polyllvm.extension.PolyLLVMIfExt;
import polyllvm.extension.PolyLLVMIntLitExt;
import polyllvm.extension.PolyLLVMLabeledExt;
import polyllvm.extension.PolyLLVMLocalAssignExt;
import polyllvm.extension.PolyLLVMLocalDeclExt;
import polyllvm.extension.PolyLLVMLocalExt;
import polyllvm.extension.PolyLLVMMethodDeclExt;
import polyllvm.extension.PolyLLVMNewArrayExt;
import polyllvm.extension.PolyLLVMNewExt;
import polyllvm.extension.PolyLLVMNullLitExt;
import polyllvm.extension.PolyLLVMProcedureDeclExt;
import polyllvm.extension.PolyLLVMReturnExt;
import polyllvm.extension.PolyLLVMSourceFileExt;
import polyllvm.extension.PolyLLVMSpecialExt;
import polyllvm.extension.PolyLLVMStringLitExt;
import polyllvm.extension.PolyLLVMTypeNodeExt;
import polyllvm.extension.PolyLLVMUnaryExt;
import polyllvm.extension.PolyLLVMWhileExt;

public final class PolyLLVMExtFactory_c extends PolyLLVMAbstractExtFactory_c {

    public PolyLLVMExtFactory_c() {
        super();
    }

    public PolyLLVMExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        return new PolyLLVMExt();
    }

    // TODO: Override factory methods for new extension nodes in the current
    // extension.

    @Override
    protected Ext extStringLitImpl() {
        return new PolyLLVMStringLitExt();
    }

    @Override
    protected Ext extIntLitImpl() {
        return new PolyLLVMIntLitExt();
    }

    @Override
    protected Ext extLocalDeclImpl() {
        return new PolyLLVMLocalDeclExt();
    }

    @Override
    protected Ext extAssignImpl() {
        return new PolyLLVMAssignExt();
    }

    @Override
    protected Ext extBinaryImpl() {
        return new PolyLLVMBinaryExt();
    }

    @Override
    protected Ext extLocalImpl() {
        return new PolyLLVMLocalExt();
    }

    @Override
    protected Ext extBlockImpl() {
        return new PolyLLVMBlockExt();
    }

    @Override
    protected Ext extEvalImpl() {
        return new PolyLLVMEvalExt();
    }

    @Override
    protected Ext extMethodDeclImpl() {
        return new PolyLLVMMethodDeclExt();
    }

    @Override
    protected Ext extTypeNodeImpl() {
        return new PolyLLVMTypeNodeExt();
    }

    @Override
    protected Ext extCanonicalTypeNodeImpl() {
        return new PolyLLVMCanonicalTypeNodeExt();
    }

    @Override
    protected Ext extFormalImpl() {
        return new PolyLLVMFormalExt();
    }

    @Override
    protected Ext extReturnImpl() {
        return new PolyLLVMReturnExt();
    }

    @Override
    protected Ext extClassBodyImpl() {
        return new PolyLLVMClassBodyExt();
    }

    @Override
    protected Ext extSourceFileImpl() {
        return new PolyLLVMSourceFileExt();
    }

    @Override
    protected Ext extClassDeclImpl() {
        return new PolyLLVMClassDeclExt();
    }

    @Override
    protected Ext extCallImpl() {
        return new PolyLLVMCallExt();
    }

    @Override
    protected Ext extIfImpl() {
        return new PolyLLVMIfExt();
    }

    @Override
    protected Ext extBooleanLitImpl() {
        return new PolyLLVMBooleanLitExt();
    }

    @Override
    protected Ext extWhileImpl() {
        return new PolyLLVMWhileExt();
    }

    @Override
    protected Ext extUnaryImpl() {
        return new PolyLLVMUnaryExt();
    }

    @Override
    protected Ext extCharLitImpl() {
        return new PolyLLVMCharLitExt();
    }

    @Override
    protected Ext extCastImpl() {
        return new PolyLLVMCastExt();
    }

    @Override
    protected Ext extBranchImpl() {
        return new PolyLLVMBranchExt();
    }

    @Override
    protected Ext extLabeledImpl() {
        return new PolyLLVMLabeledExt();
    }

    @Override
    protected Ext extFloatLitImpl() {
        return new PolyLLVMFloatLitExt();
    }

    @Override
    protected Ext extNullLitImpl() {
        return new PolyLLVMNullLitExt();
    }

    @Override
    protected Ext extNewImpl() {
        return new PolyLLVMNewExt();
    }

    @Override
    protected Ext extFieldImpl() {
        return new PolyLLVMFieldExt();
    }

    @Override
    protected Ext extSpecialImpl() {
        return new PolyLLVMSpecialExt();
    }

    @Override
    protected Ext extFieldAssignImpl() {
        return new PolyLLVMFieldAssignExt();
    }

    @Override
    protected Ext extLocalAssignImpl() {
        return new PolyLLVMLocalAssignExt();
    }

    @Override
    protected Ext extNewArrayImpl() {
        return new PolyLLVMNewArrayExt();
    }

    @Override
    protected Ext extProcedureDeclImpl() {
        return new PolyLLVMProcedureDeclExt();
    }

    @Override
    protected Ext extConstructorDeclImpl() {
        return new PolyLLVMConstructorDeclExt();
    }

    @Override
    protected Ext extConstructorCallImpl() {
        return new PolyLLVMConstructorCallExt();
    }

    @Override
    protected Ext extArrayAccessImpl() {
        return new PolyLLVMArrayAccessExt();
    }

    @Override
    protected Ext extArrayAccessAssignImpl() {
        return new PolyLLVMArrayAccessAssignExt();
    }

    @Override
    protected Ext extEmptyImpl() {
        return new PolyLLVMEmptyExt();
    }

    @Override
    protected Ext extArrayInitImpl() {
        return new PolyLLVMArrayInitExt();
    }
}
