package polyllvm.ast;

import polyglot.ext.jl7.ast.JL7NodeFactory_c;

/**
 * NodeFactory for polyllvm extension.
 */
public class PolyLLVMNodeFactory_c extends JL7NodeFactory_c implements PolyLLVMNodeFactory {

    public PolyLLVMNodeFactory_c(PolyLLVMLang lang, PolyLLVMExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public PolyLLVMExtFactory extFactory() {
        return (PolyLLVMExtFactory) super.extFactory();
    }
}
