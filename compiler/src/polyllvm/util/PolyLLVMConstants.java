package polyllvm.util;

public class PolyLLVMConstants {
    public static String THISSTRING = "_this";
    public static String MALLOC = "malloc";

    /**
     * Offset from start of array object to the contents
     */
    public static int ARRAYELEMENTOFFSET = 2;

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

    /**
     * The default address space for all LLVM pointers.
     */
    public static int LLVM_ADDR_SPACE = 0;
}
