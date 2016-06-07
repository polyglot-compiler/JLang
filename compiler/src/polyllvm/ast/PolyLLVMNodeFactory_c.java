package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.*;

import java.util.*;

/**
 * NodeFactory for PolyLLVM extension.
 */
public class PolyLLVMNodeFactory_c extends NodeFactory_c implements PolyLLVMNodeFactory {
    public PolyLLVMNodeFactory_c(PolyLLVMLang lang, PolyLLVMExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public PolyLLVMExtFactory extFactory() {
        return (PolyLLVMExtFactory) super.extFactory();
    }

    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overridden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.
}
