package polyllvm.visit;

import polyglot.ast.*;
import polyglot.types.SystemResolver;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.OptimalCodeWriter;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

/**
 * Created by Daniel on 3/13/17.
 */
public class PrintVisitor extends NodeVisitor {

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if(n instanceof SourceFile){
            System.out.println("\u001B[46m");
            n.prettyPrint(n.lang(),System.out);
            System.out.println("\u001B[0m");

        }
        return super.leave(old, n, v);
    }

}
