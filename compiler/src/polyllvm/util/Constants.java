package polyllvm.util;

import polyglot.util.CollectionUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * PolyLLVM constants for, e.g., important method names and array data layout.
 */
public class Constants {
    public static final String RUNTIME_PACKAGE = "polyllvm.runtime";
    public static final String RUNTIME_HELPER = RUNTIME_PACKAGE + ".Helper";
    public static final String RUNTIME_ARRAY = RUNTIME_PACKAGE + ".Array";
    public static final String RUNTIME_ARRAY_TYPE = RUNTIME_ARRAY + ".Type";

    public static final String PRIMITIVE_CLASS_OBJECT_SUFFIX = "ClassObject";

    public static final String CALLOC = "GC_malloc";
    public static final String ENTRY_TRAMPOLINE = "Java_polyllvm_runtime_MainWrapper_main";
    public static final int LLVM_ADDR_SPACE = 0;

    public static final String REGISTER_CLASS_FUNC = "register_java_class";
    public static final String GET_NATIVE_FUNC = "get_java_native_func";
    public static final String PERSONALITY_FUNC = "__java_personality_v0";
    public static final String CREATE_EXCEPTION = "createUnwindException";
    public static final String THROW_EXCEPTION = "throwUnwindException";
    public static final String EXTRACT_EXCEPTION = "extractJavaExceptionObject";

    public static final Set<String> NON_INVOKE_FUNCTIONS = new HashSet<>(CollectionUtil.list(
            CALLOC, CREATE_EXCEPTION, EXTRACT_EXCEPTION
    ));

    public static final int DEBUG_INFO_VERSION = 3;
    public static final int DEBUG_DWARF_VERSION = 4;

    /**
     * The default size of the table for interfaces
     */
    public static final int INTERFACE_TABLE_SIZE = 10;

    public static final String CTOR_VAR_NAME = "llvm.global_ctors";
    public static final String JNI_ENV_VAR_NAME = "jni_JNIEnv";
}
