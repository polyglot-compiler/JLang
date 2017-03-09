package polyllvm.ast;

import polyglot.ast.Expr;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.util.Position;

/**
 * NodeFactory for polyllvm extension.
 */
public interface PolyLLVMNodeFactory extends NodeFactory {

    // Factory method for Extension factory
    PolyLLVMExtFactory PolyLLVMExtFactory();

    // TODO: Declare any factory methods for new AST nodes.
}
