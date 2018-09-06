//Copyright (C) 2017 Cornell University

package jlang.extension;

import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.ast.JLangExt;
import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;
import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JLangClassLitExt extends JLangExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ClassLit n = (ClassLit) node();
        Type t = n.typeNode().type();
        assert t.isClass();

        // We eagerly load the class if necessary. See JLS 7, section 12.4.1.
        v.utils.buildClassLoadCheck(t.toClass());

        LLVMValueRef classObj = v.utils.loadClassObject(t.toClass());
        v.addTranslation(n, classObj);
        return super.leaveTranslateLLVM(v);
    }

    @Override
    public Node desugar(DesugarLocally v) {
        ClassLit n = (ClassLit) node();
        Type t = n.typeNode().type();
        Position pos = n.position();

        if (t.isVoid() || t.isPrimitive() || t.isArray()) {
            // Call java.lang.Class.forName(...) and trust library code
            // to return the right class object.
            String name = t.isArray()
                    ? getArrayClassObjectName(t.toArray())
                    : t.toString();
            Expr classNameExpr = v.tnf.StringLit(pos, name);
            return v.tnf.StaticCall(pos, v.ts.Class(), v.ts.Class(), "forName", classNameExpr);
        }
        else {
            assert t.isClass();
            return super.desugar(v);
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
