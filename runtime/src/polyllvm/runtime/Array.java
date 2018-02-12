package polyllvm.runtime;

/**
 * Constructs single- and multi-dimensional arrays, assisted by
 * the compiler. The constructors should never be called directly
 * from Java code, since we need the compiler to allocate the
 * correct amount of memory for an array instance.
 */
class Array {
    int len;
    // Array data is only visible to the compiler.

    /**
     * Single-dimensional arrays.
     */
    Array(int len) {
        // Note that entries have already been cleared,
        // since we use calloc to allocate memory.
        this.len = len;
    }

    /**
     * Multi-dimensional arrays.
     * new Object[3][2][5] ==> new Array({3,2,5})
     */
    Array(int[] lens) {
        this(lens[0]);
        initSubArrays(this, lens, 0, lens.length);
    }

    /**
     * Recursively initialize an array with `maxDepth` dimensions.
     */
    static void initSubArrays(Array arr, int[] lens, int depth, int maxDepth) {
        if (depth == maxDepth - 1) {
            // Elements already initialized to zero.
            return;
        }

        // These casts succeeds because Array and Object[] are
        // equivalent from the perspective of PolyLLVM.
        Array[] objArr = (Array[]) (Object) arr;
        for (int i = 0; i < lens[depth]; ++i) {
            Array subArr = (Array) (Object) new Object[lens[depth + 1]];
            objArr[i] = subArr;
            initSubArrays(subArr, lens, depth + 1, maxDepth);
        }
    }
}
