package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

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
}
