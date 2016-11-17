package support;

public class Array {
    private int len;

    private native void clearEntries();
    private native void setEntry(int i, Object val);

    private Array(int len) {
        this.len = len;
        clearEntries();
    }

    private Array(int[] lens, int depth, int maxDepth) {
        this.len = lens[depth];
        if (depth + 1 == maxDepth) {
            clearEntries();
        } else {
            for (int i = 0; i < lens[depth]; ++i) {
                setEntry(i, new Array(lens, depth + 1, maxDepth));
            }
        }
    }

    /**
     * The constructor for multidimensional arrays.
     * Translation: new Object[3][2][5] ==> new Array({3,2,5})
     */
    private Array(int[] lens) {
        this(lens, 0, lens.length);
    }
}
