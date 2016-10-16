package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

public class PolyLLVMMangler {

    public static String mangleProcedureName(ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            return mangleMethodName((MethodInstance) pi);
        }
        else if (pi instanceof ConstructorInstance) {
            return mangleConstructorName((ConstructorInstance) pi);
        }
        else {
            throw new InternalCompilerError("Unknown procedure type: " + pi);
        }
    }

    public static String mangleMethodName(MethodInstance mi) {
        ReferenceType container = mi.container();

        StringBuilder sb = new StringBuilder("_J_");
        sb.append(container.toString().length());
        sb.append(container.toString());
        sb.append("_");
        sb.append(mi.name().length());
        sb.append(mi.name());
        if (mi.formalTypes().isEmpty()) {
            sb.append("_void");
        }
        for (Type t : mi.formalTypes()) {
            sb.append(mangleFormalType(t));
        }

        return sb.toString();//"_" + container.toString() + "_" + mi.name();

    }

    private static String mangleConstructorName(ConstructorInstance ci) {
        ReferenceType container = ci.container();
        StringBuilder sb = new StringBuilder("_J_");
        sb.append(container.toString().length());
        sb.append(container.toString());
        sb.append("__constructor_");

        if (ci.formalTypes().isEmpty()) {
            sb.append("_void");
        }
        for (Type t : ci.formalTypes()) {
            sb.append(mangleFormalType(t));
        }
        return sb.toString();
    }

    private static String mangleFormalType(Type t) {
        StringBuilder sb = new StringBuilder();
        if (t.isArray()) {
            sb.append("_a");
            sb.append(mangleFormalType(t.toArray().base()));
//
//            sb.append(t.toArray().base().toString().length());
//            sb.append(t.toArray().base().toString());
        }
        else if (t.isReference()) {
            sb.append("_");
            sb.append(t.toString().length());
            sb.append(t.toString());
        }
        else if (t.isLongOrLess()) {
            sb.append("_i");
            sb.append(PolyLLVMTypeUtils.numBitsOfIntegralType(t));
        }
        else if (t.isBoolean()) {
            sb.append("_b");
        }
        else if (t.isFloat()) {
            sb.append("_f");

        }
        else if (t.isDouble()) {
            sb.append("_d");

        }
        else {
            throw new InternalCompilerError("Type " + t
                    + " is not properly supported");
        }
        return sb.toString();
    }

    public static String sizeVariable(ClassDecl n) {
        return sizeVariable(n.type());
    }

    public static String sizeVariable(TypeNode superClass) {
        return sizeVariable((ReferenceType) superClass.type());
    }

    public static String sizeVariable(ReferenceType superClass) {
        return "_J_size_" + superClass.toString().length()
                + superClass.toString();
    }

    public static String dispatchVectorVariable(ClassDecl n) {
        return dispatchVectorVariable(n.type());
    }

    public static String dispatchVectorVariable(TypeNode n) {
        return dispatchVectorVariable((ReferenceType) n.type());
    }

    public static String dispatchVectorVariable(ReferenceType rt) {
        return "_J_dv_" + rt.toString().length() + rt.toString();
    }

    public static String classTypeName(ClassDecl cd) {
        return classTypeName(cd.type());//"class." + cd.name();

    }

    public static String classTypeName(TypeNode superClass) {
        return classTypeName((ReferenceType) superClass.type());//"class." + superClass.name();
    }

    public static String classTypeName(ReferenceType rt) {
        if (rt.isArray()) {
            return "class.support.Array";
        }

        return "class." + rt.toString();
    }

    public static String dispatchVectorTypeName(ClassDecl cd) {
        return dispatchVectorTypeName(cd.type());//"dv." + cd.type().toString();
    }

    public static String dispatchVectorTypeName(TypeNode superClass) {
        return dispatchVectorTypeName((ReferenceType) superClass.type());//"dv." + superClass.name();
    }

    public static String dispatchVectorTypeName(ReferenceType rt) {
        return "dv." + rt.toString();
    }

    public static String classInitFunction(ClassDecl n) {
        return classInitFunction(n.type());//"_J_init_" + n.name().length() + n.name();
    }

    public static String classInitFunction(TypeNode n) {
        return classInitFunction((ReferenceType) n.type());//"_J_init_" + n.name().length() + n.name();
    }

    public static String classInitFunction(ReferenceType n) {
        return "_J_init_" + n.toString().length() + n.toString();
    }

}
