package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

public class PolyLLVMMangler {
    private static final String JAVA_PREFIX        = "Java";
    private static final String ENV_PREFIX         = "Env";
    private static final String CLASS_TYPE_STR     = "class";
    private static final String INTERFACE_TYPE_STR = "interface";
    private static final String DV_TYPE_STR        = "dv";
    private static final String IT_DV_TYPE_STR     = "ittable";
    private static final String SIZE_STR           = "size";
    private static final String DV_STR             = "dv";
    private static final String CLASS_INIT_STR     = "init";
    private static final String IT_INIT_STR        = "it_init";
    private static final String IT_STR_STR         = "ittype";

    // From the JNI API.
    private static final String UNDERSCORE_ESCAPE = "_1";
    private static final String SEMICOLON_ESCAPE  = "_2";
    private static final String BRACKET_ESCAPE    = "_3";

    /**
     * To facilitate JNI support, we mangle types as specified in the JNI API.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    private static String typeSignature(Type type) {
        if      (type.isBoolean()) return "Z";
        else if (type.isByte())    return "B";
        else if (type.isChar())    return "C";
        else if (type.isShort())   return "S";
        else if (type.isInt())     return "I";
        else if (type.isLong())    return "J";
        else if (type.isFloat())   return "F";
        else if (type.isDouble())  return "D";
        else if (type.isArray()) {
            return BRACKET_ESCAPE + typeSignature(type.toArray().base());
        }
        else if (type.isReference()) {
            return "L" + mangleQualifiedName(type.toReference()) + SEMICOLON_ESCAPE;
        }
        else {
            throw new InternalCompilerError("Unsupported type for mangling: " + type);
        }
    }

    private static String argumentSignature(ProcedureInstance pi) {
        StringBuilder sb = new StringBuilder();
        for (Type t : pi.formalTypes()) {
            sb.append(typeSignature(t));
        }
        return sb.toString();
    }

    private static String mangleName(String name) {
        return name.replace("_", UNDERSCORE_ESCAPE);
    }

    private static String mangleQualifiedName(ReferenceType classType) {
        String base = classType.isArray() ? "support.Array" : classType.toString();
        return mangleName(base).replace(".", "_");
    }

    private static String mangleProcedureName(ProcedureInstance pi,
                                              ReferenceType receiver,
                                              String procedureName) {
        return JAVA_PREFIX + "_" + mangleQualifiedName(receiver) + "_"
                + mangleName(procedureName) + "__" + argumentSignature(pi);
    }

    public static String mangleProcedureName(ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            MethodInstance mi = (MethodInstance) pi;
            return mangleProcedureName(mi, mi.container(), mi.name());
        }
        else if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            return mangleProcedureName(ci, ci.container(), ci.container().toClass().name());
        }
        else {
            throw new InternalCompilerError("Unknown procedure type: " + pi);
        }
    }

    private static String mangleStaticFieldName(ReferenceType classType, String fieldName) {
        return JAVA_PREFIX + "_" + mangleQualifiedName(classType) + "_" + mangleName(fieldName);
    }

    public static String mangleStaticFieldName(Field f) {
        return mangleStaticFieldName(f.target().type().toReference(), f.name());
    }

    public static String mangleStaticFieldName(ReferenceType classType, FieldDecl f) {
        return mangleStaticFieldName(classType, f.name());
    }

    public static String sizeVariable(ReferenceType superClass) {
        return ENV_PREFIX + "_" + mangleQualifiedName(superClass) + "_" + SIZE_STR;
    }

    public static String dispatchVectorVariable(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + DV_STR;
    }

    public static String InterfaceTableVariable(ReferenceType rt, ReferenceType i ) {
        if(i.isArray() || !(i instanceof ParsedClassType)
                || !((ParsedClassType) i).flags().isInterface()){
            throw new InternalCompilerError("Reference type " + rt + "is not an interface");
        }
        String interfaceName =  i.toString();
        String className =  rt.toString();
        return ENV_PREFIX + "it_" + interfaceName.length() + interfaceName + "_" + className.length() + className;
    }

    public static String classTypeName(ClassDecl cd) {
        return classTypeName(cd.type());
    }

    public static String classTypeName(ReferenceType rt) {
        String className = mangleQualifiedName(rt);
        if (rt instanceof  ParsedClassType && ((ParsedClassType) rt).flags().isInterface()) {
            return INTERFACE_TYPE_STR + "." + className;
        } else {
            return CLASS_TYPE_STR + "." + className;
        }
    }

    public static String dispatchVectorTypeName(ReferenceType rt) {
        String className = mangleQualifiedName(rt);
        if (rt instanceof ParsedClassType && ((ParsedClassType) rt).flags().isInterface()){
            return IT_DV_TYPE_STR + "." + className;
        } else {
            return DV_TYPE_STR + "." + className;
        }
    }

    public static String classInitFunction(ClassDecl n) {
        return classInitFunction(n.type());
    }

    public static String classInitFunction(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + CLASS_INIT_STR;
    }

    public static String interfacesInitFunction(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + IT_INIT_STR;
    }

    public static String interfaceStringVariable(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + IT_STR_STR;
    }
}
