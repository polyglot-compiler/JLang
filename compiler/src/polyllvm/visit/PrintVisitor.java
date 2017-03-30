package polyllvm.visit;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.visit.NodeVisitor;

/**
 * Debug printing.
 */
public class PrintVisitor extends NodeVisitor {

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof SourceFile) {
            System.out.println("\u001B[46m");
            n.prettyPrint(n.lang(), System.out);
            System.out.println("\u001B[0m");
        }
        return super.leave(old, n, v);
    }
}
