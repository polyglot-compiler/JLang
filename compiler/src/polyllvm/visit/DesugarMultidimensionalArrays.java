package polyllvm.visit;

import polyglot.ast.*;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.TypedNodeFactory;

import static polyllvm.util.Constants.RUNTIME_ARRAY;
import static polyllvm.util.Constants.RUNTIME_ARRAY_TYPE;

/**
 * Desugars multidimensional arrays into a runtime call, which in turn builds the multidimensional
 * array by recursively building and initializing single dimensional arrays.
 */
public class DesugarMultidimensionalArrays extends NodeVisitor {
    private final PolyLLVMTypeSystem ts;
    private final NodeFactory nf;
    private final TypedNodeFactory tnf;

    public DesugarMultidimensionalArrays(PolyLLVMTypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        tnf = new TypedNodeFactory(ts, nf);
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof NewArray) {
            try {
                return translateMultidimensionalArray((NewArray) n);
            } catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
        }
        return super.leave(old, n, v);
    }

    private Expr translateMultidimensionalArray(NewArray na) throws SemanticException {
        // Initializer expressions have their own translation.
        if (na.init() != null)
            return na;

        // We only care about multidimensional arrays.
        if (na.dims().size() <= 1)
            return na;

        Position pos = na.position();
        ClassType arrType = ts.typeForName(RUNTIME_ARRAY).toClass();
        ReferenceType leafTypeEnum = ts.typeForName(RUNTIME_ARRAY_TYPE).toReference();

        Type leafType;
        if (na.additionalDims() > 0) {
            // If there are additional dims, then the leaf arrays store null array references.
            leafType = ts.Object();
        } else {
            leafType = na.type().toArray().ultimateBase();
        }
        String leafTypeStr = getLeafTypeString(leafType);
        Field leafTypeField = tnf.StaticField(pos, leafTypeStr, leafTypeEnum, leafTypeEnum);
        ArrayInit lens = (ArrayInit) nf.ArrayInit(pos, na.dims()).type(ts.arrayOf(ts.Int()));
        return tnf.StaticCall(pos, "createMultidimensional", arrType, arrType, leafTypeField, lens);
    }

    // Returns the name of the enum constant corresponding to the given array leaf type.
    private String getLeafTypeString(Type t) {
        if (t.isBoolean())   return "BOOLEAN";
        if (t.isByte())      return "BYTE";
        if (t.isChar())      return "CHAR";
        if (t.isShort())     return "SHORT";
        if (t.isInt())       return "INT";
        if (t.isLong())      return "LONG";
        if (t.isFloat())     return "FLOAT";
        if (t.isDouble())    return "DOUBLE";
        if (t.isReference()) return "OBJECT";
        throw new InternalCompilerError("Unhandled array leaf type");
    }
}
