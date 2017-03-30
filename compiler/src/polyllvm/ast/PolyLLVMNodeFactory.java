package polyllvm.ast;

import polyglot.ext.jl7.ast.JL7NodeFactory;

/**
 * NodeFactory for polyllvm extension.
 */
public interface PolyLLVMNodeFactory extends JL7NodeFactory {

    // Factory method for Extension factory
    PolyLLVMExtFactory PolyLLVMExtFactory();

    // TODO: Declare any factory methods for new AST nodes.
}
