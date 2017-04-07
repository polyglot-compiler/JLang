package polyllvm.util;

import polyglot.util.CollectionUtil;

import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static String THIS_STR = "_this";
    public static String CALLOC = "GC_malloc";
    public static String ENTRY_TRAMPOLINE = "java_entry_point";
    public static String ARR_CLASS = "class.support_Array";
    public static int LLVM_ADDR_SPACE = 0;

    public static final String TYPEID_INTRINSIC = "llvm.eh.typeid.for";
    public static final String PERSONALITY_FUNC = "__jxx_personality_v0";
    public static final String ALLOCATE_EXCEPTION = "allocateJavaException";
    public static final String THROW_EXCEPTION = "throwJavaException";

    public static final Set<String> NON_INVOKE_FUNCTIONS = new HashSet<>(CollectionUtil.list(
                    TYPEID_INTRINSIC,
                    CALLOC,
                    ALLOCATE_EXCEPTION));

    /**
     * Offset from start of array object to the contents
     */
    public static int ARR_HEADER_SIZE = LLVMUtils.llvmPtrSize() + 4; // Type info and length.
    public static final int ARR_ELEM_OFFSET = 2;
    public static final int ARR_LEN_OFFSET = 1;

    /**
     * Offset from start of dispatch vector to the 0th method
     *  * First slot used for interface dispatch
     *  * Second slot used for type information
     */
    public static final int DISPATCH_VECTOR_OFFSET = 2;

    /**
     * The default size of the table for interfaces
     */
    public static final int INTERFACE_TABLE_SIZE = 10;

    public static final String CTOR_VAR_NAME = "llvm.global_ctors";
}
