package polyllvm.util;

public class Constants {
    public static String THIS_STR = "_this";
    public static String CALLOC = "GC_malloc";
    public static String ENTRY_TRAMPOLINE = "java_entry_point";
    public static String ARR_CLASS = "class.support_Array";
    public static int LLVM_ADDR_SPACE = 0;

    /**
     * Offset from start of array object to the contents
     */
    public static int ARR_ELEM_OFFSET = 2;

    /**
     * Offset from start of dispatch vector to the 0th method
     *  * First slot used for interface dispatch
     *  * Second slot used for type information
     */
    public static int DISPATCH_VECTOR_OFFSET = 2;

    /**
     * The default size of the table for interfaces
     */
    public static int INTERFACE_TABLE_SIZE = 10;
}
