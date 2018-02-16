package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Copies AST nodes where necessary to ensure that each node appears only once in the AST.
 * This ensures that AST nodes can be unique keys in a hash map, for example.
 *
 * An alternative is to prevent node aliasing by construction, but this is inconvenient during
 * desugar translations since we would like to avoid needing .copy() calls when reusing a node.
 */
public class RemoveNodeAliases extends NodeVisitor {
    private static final Set<Node> seen = new HashSet<>();

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (seen.contains(n))
            return n.copy();
        seen.add(n);
        return n;
    }
}
