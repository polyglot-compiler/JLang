package polyllvm.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl7.ast.JL7AbstractExtFactory_c;

public abstract class PolyLLVMAbstractExtFactory_c
        extends JL7AbstractExtFactory_c
        implements PolyLLVMExtFactory {

    public PolyLLVMAbstractExtFactory_c() {
        super();
    }

    public PolyLLVMAbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public Ext extESeq() {
        Ext e = extESeqImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof PolyLLVMExtFactory) {
                e2 = ((PolyLLVMExtFactory) nextExtFactory()).extESeq();
            }
            else {
                e2 = nextExtFactory().extExpr();
            }
            e = composeExts(e, e2);
        }
        return postExtESeq(e);
    }

    protected Ext extESeqImpl() {
        return extExprImpl();
    }

    protected Ext postExtESeq(Ext e) {
        return postExtExpr(e);
    }
}
