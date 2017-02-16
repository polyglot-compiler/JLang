package polyllvm.visit;

import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;

/**
 * De-sugars inconvenient syntactic structures.
 * Currently converts `l += r` into `l = l + r`, and analogously for -=, *=, etc.
 */
public class SugarRemover extends NodeVisitor {
    private NodeFactory nf;

    public SugarRemover(NodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
    }

    private static Binary.Operator convertAssignOpToBinop(Assign.Operator op) {
        if      (op.equals(Assign.ADD_ASSIGN))     return Binary.ADD;
        else if (op.equals(Assign.SUB_ASSIGN))     return Binary.SUB;
        else if (op.equals(Assign.MUL_ASSIGN))     return Binary.MUL;
        else if (op.equals(Assign.DIV_ASSIGN))     return Binary.DIV;
        else if (op.equals(Assign.MOD_ASSIGN))     return Binary.MOD;
        else if (op.equals(Assign.BIT_AND_ASSIGN)) return Binary.BIT_AND;
        else if (op.equals(Assign.BIT_OR_ASSIGN))  return Binary.BIT_OR;
        else if (op.equals(Assign.BIT_XOR_ASSIGN)) return Binary.BIT_XOR;
        else if (op.equals(Assign.SHL_ASSIGN))     return Binary.SHL;
        else if (op.equals(Assign.SHR_ASSIGN))     return Binary.SHR;
        else if (op.equals(Assign.USHR_ASSIGN))    return Binary.USHR;
        else throw new InternalCompilerError("Invalid assignment-to-binop conversion");
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof Assign) {
            Assign assign = (Assign) n;
            if (assign.operator().equals(Assign.ASSIGN))
                return n;
            Binary.Operator op = convertAssignOpToBinop(assign.operator());
            Binary val = nf.Binary(assign.right().position(), assign.left(), op, assign.right());
            return assign.operator(Assign.ASSIGN).right(val);
        } else {
            return n;
        }
    }
}
