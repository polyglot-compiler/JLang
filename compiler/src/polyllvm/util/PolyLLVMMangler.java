package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;

public class PolyLLVMMangler {

    public static String mangleMethodName(MethodInstance mi) {
        ReferenceType container = mi.container();
        return "_" + container.toString() + "_" + mi.name();

    }

    public static String sizeVariable(ClassDecl n) {
        return "_J_size_" + n.name().length() + n.name();
    }

    public static String sizeVariable(TypeNode superClass) {
        return "_J_size_" + superClass.name().length() + superClass.name();
    }

    public static String dispatchVectorVariable(ReferenceType rt) {
        return "_J_dv_" + rt.toString().length() + rt.toString();
    }

    public static String dispatchVectorVariable(ClassDecl n) {
        return dispatchVectorVariable(n.type());
    }

    public static String dispatchVectorVariable(TypeNode n) {
        return dispatchVectorVariable((ReferenceType) n.type());
    }

    public static String classInitFunction(ClassDecl n) {
        return "_J_init_" + n.name().length() + n.name();
    }

    public static String classTypeName(ClassDecl cd) {
        return "class." + cd.name();

    }

    public static String dispatchVectorTypeName(ClassDecl cd) {
        return "dv." + cd.name();
    }

    public static String classTypeName(TypeNode superClass) {
        return "class." + superClass.name();
    }

    public static String dispatchVectorTypeName(TypeNode superClass) {
        return "dv." + superClass.name();
    }

    public static String classTypeName(ReferenceType rt) {
        return "class." + rt.toString();
    }

    public static String dispatchVectorTypeName(ReferenceType rt) {
        return "dv." + rt.toString();
    }

}
