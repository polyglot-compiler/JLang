package polyllvm.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.JL5ClassDeclExt;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.jl5.visit.RemoveEnums;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.util.LLVMUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveJava5isms extends NodeVisitor {

    private TypeSystem ts;
    private NodeFactory nf;

    public RemoveJava5isms(TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        Position pos = n.position();
        if (n instanceof TypeNode) {
            TypeNode tn = (TypeNode) n;
                // Get the erasure type
                Type t = translateType(tn.type());
                return nf.CanonicalTypeNode(pos, translateType(t));

        } else if (n instanceof ClassDecl){
            JL5ClassDeclExt ext = (JL5ClassDeclExt) n.ext();
            return ext.paramTypes(new ArrayList<>());
        }
        return super.leave(old, n, v);
    }

    @SuppressWarnings("unchecked")
    public <T extends Type> T translateType(T t) {
        t = (T) ((JL5TypeSystem) ts).erasureType(t);
        if (t instanceof LubType) {
            t = (T) ((LubType) t).calculateLub();
            t = (T) ((JL5TypeSystem) ts).erasureType(t);
        }

        if (t instanceof JL5SubstClassType) {
            // For C<T1,...,Tn>, just print C.
            JL5SubstClassType jct = (JL5SubstClassType) t;
            return (T) translateType(jct.base());
        }
        else if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return (T) ts.arrayOf(translateType(at.base()));
        } else if (t instanceof ParsedClassType){
            ParsedClassType parsedClassType = (ParsedClassType) t;
            if (parsedClassType.outer() != null) {
                parsedClassType.outer(translateType(parsedClassType.outer()));
                return (T) parsedClassType;
            }
        }
        return t;

    }
}
