//Copyright (C) 2018 Cornell University

package jlang.util;

import jlang.visit.LLVMTranslator;
import polyglot.ast.ClassDecl;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

/** Mangles Java methods and variables for use in LLVM IR. */
public class JLangMangler {
    private static final int CODE_POINT_PADDED_LENGTH = 4;
	private final LLVMTranslator v;

    public JLangMangler(LLVMTranslator v) {
        this.v = v;
    }

    private static final String JAVA_PREFIX = "Java";
    private static final String POLYGLOT_PREFIX = "Polyglot";
    private static final String JNI_TRAMPOLINE_PREFIX = "Jni_trampoline";
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
	private static final String CODE_POINT_ESCAPE = "_0";
    private static final String UNDERSCORE_ESCAPE = "_1";
    private static final String SEMICOLON_ESCAPE = "_2";
    private static final String BRACKET_ESCAPE = "_3";

    /**
     * Mangle types as specified in the JNI API.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html#type_signatures
     */
    public String jniUnescapedSignature(Type t) {
        Type et = v.ts.erasureType(t);
        if      (et.isBoolean()) return "Z";
        else if (et.isByte())    return "B";
        else if (et.isChar())    return "C";
        else if (et.isShort())   return "S";
        else if (et.isInt())     return "I";
        else if (et.isLong())    return "J";
        else if (et.isFloat())   return "F";
        else if (et.isDouble())  return "D";
        else if (et.isVoid())    return "V";
        else if (et.isArray()) {
            return "[" + jniUnescapedSignature(et.toArray().base());
        }
        else if (et.isClass()) {
            ClassType base = (ClassType) et.toClass().declaration();
            return "L" + userVisibleClassName(base).replace('.', '/') + ";";
        }
        else {
            throw new InternalCompilerError("Unsupported type for mangling: " + et);
        }
    }

    /**
     * Returns the name of the entity (class, primitive types, array, and etc.) formatted for
     * use by Class#getName().
     * Rules to mangle the name are specified here:
     * https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName()
     *
     * E.g., package.Clazz$InnerClass
     */
    public String userVisibleEntityName(Type t) {
        Type et = v.ts.erasureType(t);
        if      (et.isBoolean()) return "boolean";
        else if (et.isByte())    return "byte";
        else if (et.isChar())    return "char";
        else if (et.isShort())   return "short";
        else if (et.isInt())     return "int";
        else if (et.isLong())    return "long";
        else if (et.isFloat())   return "float";
        else if (et.isDouble())  return "double";
        else if (et.isVoid())    return "void";
        else if (et.isArray()) {
            // jni replace "." with "/" so we need to revert it here.
            return jniUnescapedSignature(et).replace("/", ".");
        }
        else if (et.isClass()) {
            ClassType base = (ClassType) et.toClass().declaration();
            return userVisibleClassName(base);
        }
        else {
            throw new InternalCompilerError("Unsupported type for mangling: " + et);
        }
    }

    private boolean isValidCFuncChar(char c) {
    	return Character.isDigit(c) || Character.isLetter(c) || c == '_';
    }

    private String escapeSignature(String signature) {
        String sig = signature
                .replace("_", UNDERSCORE_ESCAPE)
                .replace("/", "_")
                .replace(".", "_") //hacky -> part of JLang not Java spec. TODO is refactor code to use different escapes
                .replace(";", SEMICOLON_ESCAPE)
                .replace("[", BRACKET_ESCAPE);
        StringBuffer result = new StringBuffer(sig.length());
        for (int i = 0; i < sig.length(); i++) {
        	char c = sig.charAt(i);
        	if (isValidCFuncChar(c)) {
        		result.append(c);
        	} else {
        		result.append(CODE_POINT_ESCAPE);
        		String codePoint = Integer.toHexString(sig.codePointAt(i)).toLowerCase();
        		int zeroesToPrepend = CODE_POINT_PADDED_LENGTH - codePoint.length() ;
        		while (zeroesToPrepend > 0) {
        			result.append("0");
        			zeroesToPrepend -= 1;
        		}
				result.append(codePoint);
        	}
        }
        return result.toString();
    }

    /**
     * Returns a class name formatted for use by Class#getName() and Class#forName(...).
     * E.g., package.Clazz$InnerClass
     */
    public String userVisibleClassName(ClassType t) {
        return t.outer() != null
                ? userVisibleClassName(t.outer()) + "$" + t.name()
                : t.fullName();
    }

    /**
     * Returns the type signature of the given procedure for JNI purposes.
     * E.g., (ILjava/lang/String;[I)J
     */
    public String jniUnescapedSignature(ProcedureInstance pi) {
        pi = v.utils.erasedProcedureInstance(pi); // Erase generics.
        Type returnType = v.utils.erasedReturnType(pi);
        String formalTypeSignature = pi.formalTypes().stream()
                .map(this::jniUnescapedSignature)
                .reduce("", (a, b) -> a + b);
        return "(" + formalTypeSignature + ")" + jniUnescapedSignature(returnType);
    }

    /**
     * Mangle types as specified in the JNI API, escaped for symbol table purposes.
     * https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    private String jniEscapedSignature(Type t) {
        return escapeSignature(jniUnescapedSignature(t));
    }

    /**
     * Similar to {@link JLangMangler#jniUnescapedSignature(Type)}, but
     * merges types with the same calling convention treatment.
     */
    private String callingConventionSignature(Type t) {
        return t.isReference() ? "L" : jniUnescapedSignature(t);
    }

    /**
     * Similar to {@link JLangMangler#jniUnescapedSignature(ProcedureInstance)}, but
     * merges signatures with the same calling convention.
     */
    public String callingConventionSignature(ProcedureInstance pi) {
        pi = v.utils.erasedProcedureInstance(pi); // Erase generics.
        Type returnType = v.utils.erasedReturnType(pi);
        String formalTypeSignature = v.utils.erasedImplicitFormalTypes(pi).stream()
                .map(this::callingConventionSignature)
                .reduce("", (a, b) -> a + b);
        return "(" + formalTypeSignature + ")" + callingConventionSignature(returnType);
    }

    private String escapedName(String name) {
        return name.replace("_", UNDERSCORE_ESCAPE);
    }

    private String qualifiedName(ReferenceType t, boolean mangleEscapes) {
        ClassType erasure = v.utils.erasureLL(t);
        ClassType base = (ClassType) erasure.declaration();
        String baseName = userVisibleClassName(base);
        return (mangleEscapes) ?
        		escapeSignature(baseName) :
        			baseName.replace('.', '_');
    }

    private String procSuffix(ProcedureInstance pi, String name, boolean abbreviated, boolean mangleUnicode) {
        StringBuilder sb = new StringBuilder();
        sb.append(qualifiedName(pi.container(), mangleUnicode));
        sb.append('_');
        sb.append(escapedName(name));
        if (!abbreviated) {
            // Add argument type information.
            sb.append("__");
            for (Type t : pi.formalTypes()) {
                sb.append(jniEscapedSignature(t));
            }
        }
        return sb.toString();
    }

    private String procSuffix(ProcedureInstance pi, boolean abbreviated, boolean mangleUnicode) {
        if (pi instanceof MethodInstance) {
            MethodInstance mi = (MethodInstance) pi;
            return procSuffix(mi.orig(), mi.name(), abbreviated, mangleUnicode);
        }
        else if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            return procSuffix(ci.orig(), ci.container().toClass().name(), abbreviated, mangleUnicode);
        }
        else {
            throw new InternalCompilerError("Unknown procedure type: " + pi.getClass());
        }
    }

    public String procJniTrampoline(ProcedureInstance pi) {
        return JNI_TRAMPOLINE_PREFIX + "_" + callingConventionSignature(pi);
    }

    public String proc(ProcedureInstance pi) {
        return POLYGLOT_PREFIX + "_" + procSuffix(pi, /*abbreviated*/ false, false);
    }

    public String shortNativeSymbol(ProcedureInstance pi) {
        return JAVA_PREFIX + "_" + procSuffix(pi, /*abbreviated*/ true, true);
    }

    public String longNativeSymbol(ProcedureInstance pi) {
        return JAVA_PREFIX + "_" + procSuffix(pi, /*abbreviated*/ false, true);
    }

    public String staticField(FieldInstance fi) {
        return staticField(fi.container(), fi.name());
    }

    public String staticField(ReferenceType rt, String fieldName) {
        return POLYGLOT_PREFIX + "_" + qualifiedName(rt, false) + "_" + escapedName(fieldName);
    }

    public String idvGlobalId(ClassType intf, ReferenceType clazz) {
        return POLYGLOT_PREFIX +
                "_" + qualifiedName(intf, false) +
                "_" + qualifiedName(clazz, false) +
                "_" + IDV_STR;
    }

    public String cdvTyName(ReferenceType t) {
        String mangled = qualifiedName(t, false);
        return CDV_TYPE_STR + "." + mangled;
    }

    public String idvTyName(ClassType intf) {
        String intfMangled = qualifiedName(intf, false);
        return IDV_TYPE_STR + "." + intfMangled;
    }

    public String classTypeName(ClassDecl cd) {
        return classTypeName(cd.type());
    }

    public String classTypeName(ClassType t) {
        String className = qualifiedName(t, false);
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
        return POLYGLOT_PREFIX + "_" + qualifiedName(rt, false);
    }
}
