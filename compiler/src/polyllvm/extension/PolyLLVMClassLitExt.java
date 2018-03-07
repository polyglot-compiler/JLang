package polyllvm.extension;

import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMClassLitExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("Class literals should be desugared");
    }

    @Override
    public Node desugar(DesugarLocally v) {
        ClassLit n = (ClassLit) node();
        Type t = n.typeNode().type();
        Position pos = n.position();

        if (t.isVoid() || t.isPrimitive()) {
            // Get the class object from the runtime library.
            String fieldName = t.toString() + Constants.PRIMITIVE_CLASS_OBJECT_SUFFIX;
            return v.tnf.StaticField(pos, fieldName, v.ts.RuntimeHelper());
        }
        else if (t.isArray()) {
            // Call java.lang.Class.forName(...) and trust library code
            // to return the right class object.
            Expr classNameExpr = v.tnf.StringLit(pos, getArrayClassObjectName(t.toArray()));
            return v.tnf.StaticCall(pos, "forName", v.ts.Class(), v.ts.Class(), classNameExpr);
        }
        else if (t.isReference()) {
            // Get the class object from a static field that we insert into every class.
            return v.tnf.StaticField(pos, Constants.CLASS_OBJECT, t.toReference());
        }
        else {
            throw new InternalCompilerError("Unhandled type kind");
        }
    }

    /** Mangles class object names for array types, preferably in the same way as javac. */
    protected String getArrayClassObjectName(ArrayType t) {
        Type base = t.base();

        String baseStr;
        if      (base.isBoolean()) baseStr = "Z";
        else if (base.isByte())    baseStr = "B";
        else if (base.isChar())    baseStr = "C";
        else if (base.isShort())   baseStr = "S";
        else if (base.isInt())     baseStr = "I";
        else if (base.isLong())    baseStr = "J";
        else if (base.isFloat())   baseStr = "F";
        else if (base.isDouble())  baseStr = "D";
        else if (base.isArray())   baseStr = getArrayClassObjectName(base.toArray());
        else if (base.isClass())   baseStr = "L" + getClassObjectName(base.toClass()) + ";";
        else {
            throw new InternalCompilerError("Unhandled type kind");
        }

        return "[" + baseStr;
    }

    protected String getClassObjectName(ClassType t) {
        return t.outer() == null
                ? t.fullName()
                : getClassObjectName(t.outer()) + "$" + t.name();
    }
}
