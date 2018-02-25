package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

/**
 * Mangles Java methods and variables for use in LLVM IR.
 */
public class PolyLLVMMangler {
    private final LLVMTranslator v;

    public PolyLLVMMangler(LLVMTranslator v) {
        this.v = v;
    }

    static private final String JAVA_PREFIX = "Java";
    static private final String ENV_PREFIX = "Env";
    static private final String CLASS_TYPE_STR = "class";
    static private final String INTERFACE_TYPE_STR = "interface";
    static private final String CDV_TYPE_STR = "cdv_ty";
    static private final String IDV_TYPE_STR = "idv_ty";
    static private final String SIZE_STR = "size";
    static private final String CDV_STR = "cdv";
    static private final String IDV_STR = "idv";
    static private final String IDV_ARR_STR = "idv_arr";
    static private final String IDV_ID_ARR_STR = "idv_id_arr";
    static private final String IDV_ID_HASH_ARR_STR = "idv_hash_arr";
    static private final String CLASS_INIT_STR = "init";
    static private final String IT_INIT_STR = "it_init";
    static private final String IT_STR_STR = "intf_name";
    static private final String TYPE_INFO_STR = "rtti";
    static private final String CLASS_ID_STR = "class_id";

    // From the JNI API.
    static private final String UNDERSCORE_ESCAPE = "_1";
    static private final String SEMICOLON_ESCAPE = "_2";
    static private final String BRACKET_ESCAPE = "_3";

    /**
     * To facilitate JNI support, we mangle types as specified in the JNI API.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    private String typeSignature(Type t) {
        Type et = v.typeSystem().erasureType(t);
        if      (et.isBoolean()) return "Z";
        else if (et.isByte())    return "B";
        else if (et.isChar())    return "C";
        else if (et.isShort())   return "S";
        else if (et.isInt())     return "I";
        else if (et.isLong())    return "J";
        else if (et.isFloat())   return "F";
        else if (et.isDouble())  return "D";
        else if (et.isArray())
            return BRACKET_ESCAPE + typeSignature(et.toArray().base());
        else if (et.isClass())
            return "L" + mangleQualifiedName(et.toClass()) + SEMICOLON_ESCAPE;
        else
            throw new InternalCompilerError("Unsupported type for mangling: " + et);
    }

    private String mangleName(String name) {
        return name.replace("_", UNDERSCORE_ESCAPE);
    }

    private String mangleQualifiedName(ReferenceType t) {
        ClassType erasure = v.utils.erasureLL(t);
        ParsedClassType base = (ParsedClassType) erasure.declaration();
        if (base.outer() != null) {
            return mangleQualifiedName(base.outer()) + "_" + base.name();
        } else {
            return base.fullName().replace('.', '_');
        }
    }

    private String mangleProcName(ProcedureInstance pi, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(JAVA_PREFIX);
        sb.append('_');
        sb.append(mangleQualifiedName(pi.container()));
        sb.append('_');
        sb.append(mangleName(name));
        sb.append("__");
        for (Type t : pi.formalTypes())
            sb.append(typeSignature(t));
        return sb.toString();
    }

    public String mangleProcName(ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            MethodInstance mi = (MethodInstance) pi;
            return mangleProcName(mi.orig(), mi.name());
        } else if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            return mangleProcName(ci.orig(), ci.container().toClass().name());
        } else {
            throw new InternalCompilerError("Unknown procedure type: " + pi.getClass());
        }
    }

    public String mangleStaticFieldName(FieldInstance fi) {
        return JAVA_PREFIX + "_" + mangleQualifiedName(fi.container()) + "_"
                + mangleName(fi.name());
    }

    public String sizeVariable(ReferenceType superClass) {
        return ENV_PREFIX + "_" + mangleQualifiedName(superClass) + "_"
                + SIZE_STR;
    }

    public String cdvGlobalId(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + CDV_STR;
    }

    public String idvArrGlobalId(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + IDV_ARR_STR;
    }

    public String idvIdArrGlobalId(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_"
                + IDV_ID_ARR_STR;
    }

    public String idvIdHashArrGlobalId(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_"
                + IDV_ID_HASH_ARR_STR;
    }

    public String idvGlobalId(ClassType intf, ReferenceType clazz) {
        String intfStr = v.utils.erasureLL(intf).toString();
        String clazzStr = v.utils.erasureLL(clazz).toString();
        return ENV_PREFIX + "_" + intfStr.length() + intfStr + "_"
                + clazzStr.length() + clazzStr + "_" + IDV_STR;
    }

    public String cdvTyName(ReferenceType t) {
        String mangled = mangleQualifiedName(t);
        return CDV_TYPE_STR + "." + mangled;
    }

    public String idvTyName(ClassType intf) {
        String intfMangled = mangleQualifiedName(intf);
        return IDV_TYPE_STR + "." + intfMangled;
    }

    public String classTypeName(ClassDecl cd) {
        return classTypeName(cd.type());
    }

    public String classTypeName(ReferenceType t) {
        String className = mangleQualifiedName(t);
        if (v.utils.erasureLL(t).flags().isInterface()) {
            return INTERFACE_TYPE_STR + "." + className;
        } else {
            return CLASS_TYPE_STR + "." + className;
        }
    }

    public String classInitFunction(ClassDecl n) {
        return classInitFunction(n.type());
    }

    public String classInitFunction(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_"
                + CLASS_INIT_STR;
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

    public String typeIdentityId(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt) + "_" + CLASS_ID_STR;
    }
}
