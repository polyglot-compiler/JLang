package polyllvm.util;

import polyglot.util.CollectionUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * PolyLLVM constants for, e.g., important method names and array data layout.
 */
public class Constants {
    public static final String THIS_STR = "_this";
    public static final String CALLOC = "GC_malloc";
    public static final String ENTRY_TRAMPOLINE = "java_entry_point";
    public static final String ARR_CLASS = "class.support_Array";
    public static final int LLVM_ADDR_SPACE = 0;

    public static final String TYPEID_INTRINSIC = "llvm.eh.typeid.for";
    public static final String PERSONALITY_FUNC = "__java_personality_v0";
    public static final String ALLOCATE_EXCEPTION = "allocateJavaException";
    public static final String THROW_EXCEPTION = "throwJavaException";

    public static final Set<String> NON_INVOKE_FUNCTIONS = new HashSet<>(CollectionUtil.list(
                    TYPEID_INTRINSIC,
                    CALLOC,
                    ALLOCATE_EXCEPTION));

    /**
     * Offset from start of Object to the 0th field
     *  * First slot used for dispatch vector
     *  * Second slot used for synchronization variable struct pointer
     */
    public static final int OBJECT_FIELDS_OFFSET = 2;

    /**
     * Offset from start of object to the class dispatch vector
     */
    public static final int DISPATCH_VECTOR_OFFSET = 0;

    /** Offset from start of array object to the length field */
    public static final int ARR_LEN_OFFSET = OBJECT_FIELDS_OFFSET; //Length is immediately after object header
    /** Offset from start of array object to start of elements */
    public static final int ARR_ELEM_OFFSET = OBJECT_FIELDS_OFFSET+1; // Element is after the length field

    /**
     * Offset from start of a class dispatch vector to the 0th method. First
     * slot used for interface dispatch. Second slot used for type information.
     */
    public static final int CLASS_DISP_VEC_OFFSET = 2;
    /**
     * Offset from start of an interface dispatch vector to the 0th method.
     */
    public static final int INTF_DISP_VEC_OFFSET = 0;


    /**
     * The default size of the table for interfaces
     */
    public static final int INTERFACE_TABLE_SIZE = 10;

    public static final String CTOR_VAR_NAME = "llvm.global_ctors";
}
