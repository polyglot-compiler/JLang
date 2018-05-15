package polyllvm.util;

import polyglot.ast.ClassDecl;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

/** Mangles Java methods and variables for use in LLVM IR. */
public class PolyLLVMMangler {
    private final LLVMTranslator v;

    public PolyLLVMMangler(LLVMTranslator v) {
        this.v = v;
    }

    private static final String JAVA_PREFIX = "Java";
    private static final String ENV_PREFIX = "Env";
    private static final String CLASS_TYPE_STR = "class";
    private static final String INTERFACE_TYPE_STR = "interface";
    private static final String CDV_TYPE_STR = "cdv_ty";
    private static final String IDV_TYPE_STR = "idv_ty";
    private static final String CDV_STR = "cdv";
    private static final String IDV_STR = "idv";
    private static final String IDV_ARR_STR = "idv_arr";
    private static final String IDV_ID_ARR_STR = "idv_id_arr";
    private static final String IDV_ID_HASH_ARR_STR = "idv_hash_arr";
    private static final String IT_INIT_STR = "it_init";
    private static final String IT_STR_STR = "intf_name";
    private static final String TYPE_INFO_STR = "rtti";
    private static final String CLASS_ID_STR = "class_id";
    private static final String CLASS_STR = "class";
    private static final String CLASS_INFO_STR = "class_info";
    private static final String LOAD_CLASS_STR = "load_class";

    // From the JNI API.
    private static final String UNDERSCORE_ESCAPE = "_1";
    private static final String SEMICOLON_ESCAPE = "_2";
    private static final String BRACKET_ESCAPE = "_3";

    /**
     * To facilitate JNI support, we mangle types as specified in the JNI API.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    private String typeSignature(Type t) {
        Type et = v.ts.erasureType(t);
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

    /**
     * Mangles a procedure name.
     * Native methods that are not overloaded by other native methods must use the
     * abbreviated mangling format, which omits argument type information.
     */
    private String mangleProcName(ProcedureInstance pi, String name, boolean abbreviated) {
        StringBuilder sb = new StringBuilder();
        sb.append(JAVA_PREFIX);
        sb.append('_');
        sb.append(mangleQualifiedName(pi.container()));
        sb.append('_');
        sb.append(mangleName(name));
        if (!abbreviated) {
            // Add argument type information as necessary.
            sb.append("__");
            for (Type t : pi.formalTypes()) {
                sb.append(typeSignature(t));
            }
        }
        return sb.toString();
    }

    public String mangleProcName(ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            MethodInstance mi = (MethodInstance) pi;
            boolean abbreviated = mi.flags().isNative() &&
                    mi.container().methodsNamed(mi.name()).stream()
                            .filter(m -> m.flags().isNative())
                            .count() <= 1;
            return mangleProcName(mi.orig(), mi.name(), abbreviated);
        }
        else if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            boolean abbreviated = ci.flags().isNative() &&
                    ci.container().toClass().constructors().stream()
                            .filter(c -> c.flags().isNative())
                            .count() <= 1;
            return mangleProcName(ci.orig(), ci.container().toClass().name(), abbreviated);
        }
        else {
            throw new InternalCompilerError("Unknown procedure type: " + pi.getClass());
        }
    }

    public String mangleStaticFieldName(FieldInstance fi) {
        return mangleStaticFieldName(fi.container(), fi.name());
    }

    public String mangleStaticFieldName(ReferenceType rt, String fieldName) {
        return JAVA_PREFIX + "_" + mangleQualifiedName(rt) + "_" + mangleName(fieldName);
    }

    public String idvGlobalId(ClassType intf, ReferenceType clazz) {
        return ENV_PREFIX +
                "_" + mangleQualifiedName(intf) +
                "_" + mangleQualifiedName(clazz) +
                "_" + IDV_STR;
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

    public String classTypeName(ClassType t) {
        String className = mangleQualifiedName(t);
        String prefix = v.utils.erasureLL(t).flags().isInterface()
                ? INTERFACE_TYPE_STR
                : CLASS_TYPE_STR;
        return prefix + "." + className;
    }

    public String cdvGlobalId(ReferenceType rt) {
        return classSpecificGlobal(rt, CDV_STR);
    }

    public String idvArrGlobalId(ReferenceType rt) {
        return classSpecificGlobal(rt, IDV_ARR_STR);
    }

    public String idvIdArrGlobalId(ReferenceType rt) {
        return classSpecificGlobal(rt, IDV_ID_ARR_STR);
    }

    public String idvIdHashArrGlobalId(ReferenceType rt) {
        return classSpecificGlobal(rt, IDV_ID_HASH_ARR_STR);
    }

    public String interfacesInitFunction(ReferenceType rt) {
        return classSpecificGlobal(rt, IT_INIT_STR);
    }

    public String interfaceStringVariable(ReferenceType rt) {
        return classSpecificGlobal(rt, IT_STR_STR);
    }

    public String typeInfo(ReferenceType rt) {
        return classSpecificGlobal(rt, TYPE_INFO_STR);
    }

    public String typeIdentityId(ReferenceType rt) {
        return classSpecificGlobal(rt, CLASS_ID_STR);
    }

    public String classObj(ClassType ct) {
        return classSpecificGlobal(ct, CLASS_STR);
    }

    public String classInfoGlobal(ClassType ct) {
        return classSpecificGlobal(ct, CLASS_INFO_STR);
    }

    public String classLoadingFunc(ClassType ct) {
        return classSpecificGlobal(ct, LOAD_CLASS_STR);
    }

    private String classSpecificGlobal(ReferenceType rt, String suffix) {
        return typePrefix(rt) + "_" + suffix;
    }

    private String typePrefix(ReferenceType rt) {
        return ENV_PREFIX + "_" + mangleQualifiedName(rt);
    }
}
