package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMMangler {
    private final LLVMTranslator v;

    public PolyLLVMMangler(LLVMTranslator v) {
        this.v = v;
    }

    private final String JAVA_PREFIX        = "Java";
    private final String ENV_PREFIX         = "Env";
    private final String CLASS_TYPE_STR     = "class";
    private final String INTERFACE_TYPE_STR = "interface";
    private final String DV_TYPE_STR        = "dv";
    private final String IT_DV_TYPE_STR     = "ittable";
    private final String SIZE_STR           = "size";
    private final String DV_STR             = "dv";
    private final String CLASS_INIT_STR     = "init";
    private final String IT_INIT_STR        = "it_init";
    private final String IT_STR_STR         = "ittype";
    private final String TYPE_INFO_STR      = "type_info";
    private final String CLASS_ID_STR       = "class_id";

    // From the JNI API.
    private final String UNDERSCORE_ESCAPE = "_1";
    private final String SEMICOLON_ESCAPE  = "_2";
    private final String BRACKET_ESCAPE    = "_3";

    /**
     * To facilitate JNI support, we mangle types as specified in the JNI API.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    private String typeSignature(Type type) {
        type = v.utils.translateType(type);

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

    private String argumentSignature(ProcedureInstance pi) {
        StringBuilder sb = new StringBuilder();
        for (Type t : pi.formalTypes()) {
            sb.append(typeSignature(t));
        }
        return sb.toString();
    }

    private String mangleName(String name) {
        return name.replace("_", UNDERSCORE_ESCAPE);
    }

    private String mangleQualifiedName(ReferenceType classType) {
        classType = v.utils.translateType(classType);
        String base = classType.isArray() ? "support.Array" : classType.toString();
        return mangleName(base).replace(".", "_");
    }

    private String mangleProcedureName(ProcedureInstance pi,
                                       ReferenceType receiver,
                                       String procedureName) {
        return JAVA_PREFIX + "_" + mangleQualifiedName(receiver) + "_"
                + mangleName(procedureName) + "__" + argumentSignature(pi);
    }

    public String mangleProcedureName(ProcedureInstance pi) {
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

    private String mangleStaticFieldName(ReferenceType classType, String fieldName) {
        classType = v.utils.translateType(classType);
        return JAVA_PREFIX + "_" + mangleQualifiedName(classType) + "_" + mangleName(fieldName);
    }

    public String mangleStaticFieldName(Field f) {
        return mangleStaticFieldName(f.target().type().toReference(), f.name());
    }

    public String mangleStaticFieldName(ReferenceType classType, FieldDecl f) {
        return mangleStaticFieldName(classType, f.name());
    }

    public String sizeVariable(ReferenceType superClass) {
        return ENV_PREFIX + "_" + mangleQualifiedName(superClass) + "_" + SIZE_STR;
    }

    public String dispatchVectorVariable(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + DV_STR;
    }

    public String InterfaceTableVariable(ReferenceType rt, ReferenceType i ) {
        if (i.isArray() || !(i instanceof ParsedClassType)
                || !((ParsedClassType) i).flags().isInterface()) {
            throw new InternalCompilerError("Reference type " + rt + "is not an interface");
        }
        rt = v.utils.translateType(rt);
        i = v.utils.translateType(i);

        String interfaceName =  i.toString();
        String className =  rt.toString();
        return ENV_PREFIX + "it_" + interfaceName.length() + interfaceName + "_" + className.length() + className;
    }

    public String classTypeName(ClassDecl cd) {
        return classTypeName(cd.type());
    }

    public String classTypeName(ReferenceType rt) {
        String className = mangleQualifiedName(rt);
        if (rt instanceof  ParsedClassType && ((ParsedClassType) rt).flags().isInterface()) {
            return INTERFACE_TYPE_STR + "." + className;
        } else {
            return CLASS_TYPE_STR + "." + className;
        }
    }

    public String dispatchVectorTypeName(ReferenceType rt) {
        String className = mangleQualifiedName(rt);
        if (rt instanceof ParsedClassType && ((ParsedClassType) rt).flags().isInterface()) {
            return IT_DV_TYPE_STR + "." + className;
        } else {
            return DV_TYPE_STR + "." + className;
        }
    }

    public String classInitFunction(ClassDecl n) {
        return classInitFunction(n.type());
    }

    public String classInitFunction(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + CLASS_INIT_STR;
    }

    public String interfacesInitFunction(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + IT_INIT_STR;
    }

    public String interfaceStringVariable(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + IT_STR_STR;
    }

    public String classObjName(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + TYPE_INFO_STR;
    }

    public String classIdName(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + CLASS_ID_STR;
    }
}
