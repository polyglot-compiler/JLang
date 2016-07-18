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

    public static String dispatchVectorVariable(ClassDecl n) {
        return "_J_dv_" + n.name().length() + n.name();
    }

    public static String dispatchVectorVariable(TypeNode n) {
        return "_J_dv_" + n.name().length() + n.name();
    }

    public static String classInitFunction(ClassDecl n) {
        return "_J_init_" + n.name().length() + n.name();
    }

}
