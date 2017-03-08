package polyllvm.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl7.ast.JL7AbstractExtFactory_c;

public abstract class PolyLLVMAbstractExtFactory_c extends JL7AbstractExtFactory_c
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

}
