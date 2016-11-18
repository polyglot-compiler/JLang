package support;

/**
 * Constructs single- and multi-dimensional arrays, assisted by
 * native code. The constructors should never be called directly
 * from Java code, since we need the compiler to allocate the
 * correct amount of memory for an array instance.
 */
public class Array {
    private int len;
    // Array data is only visible to native code.

    private native void clearEntries();
    private native void setObjectEntry(int i, Object val);

    /**
     * Single-dimensional arrays.
     */
    private Array(int len) {
        this.len = len;
        clearEntries();
    }

    /**
     * Multi-dimensional arrays.
     * new Object[3][2][5] ==> new Array({3,2,5})
     */
    private Array(int[] lens) {
        this(lens[0]);
        initSubArrays(this, lens, 0, lens.length);
    }

    /**
     * Recursively initialize an array with `maxDepth` dimensions.
     */
    private static void initSubArrays(Array arr, int[] lens,
                                     int depth, int maxDepth) {
        if (depth == maxDepth - 1) {
            // Note that entries have already been cleared, since
            // the single-dimensional array constructor is called
            // for every sub-array.
            return;
        }

        for (int i = 0; i < lens[depth]; ++i) {
            // This cast succeeds because Array and Object[] are
            // equivalent from the perspective of PolyLLVM.
            Array subArr = (Array) (Object) new Object[lens[depth + 1]];
            arr.setObjectEntry(i, subArr);
            initSubArrays(subArr, lens, depth + 1, maxDepth);
        }
    }
}
