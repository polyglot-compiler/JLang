package polyllvm.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

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
    public final Ext extESeq() {
        Ext e = extESeqImpl();

        ExtFactory nextEF = nextExtFactory();
        Ext e2;
        if (nextEF instanceof PolyLLVMExtFactory) {
            e2 = ((PolyLLVMExtFactory) nextEF).extESeq();
        }
        else {
            e2 = nextEF.extNode();
        }
        e = composeExts(e, e2);
        return postExtESeq(e);
    }

    protected Ext extESeqImpl() {
        return extNodeImpl();
    }

    protected Ext postExtESeq(Ext e) {
        return postExtNode(e);
    }
}
