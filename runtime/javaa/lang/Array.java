package javaa.lang;

import java.util.Arrays;

public class Array {

    public final int length;
    public final Object[] contents;

    private Array(int length) {
        this.length = length;
        contents = new Object[length];
    }

    /*
     * Adds a function for initializing arrays to the given IRCompUnit
     * equivalent to the following C code:<br>
     *
     * int* __initialize(int* l, int len) {<br>
     *              a = malloc((*l + 1) * 8);<br>
     *              *a = *l;<br>
     *              a = a + 8;<br>
     *              if (len == 1) return a;<br>
     *              int i = 0;<br>
     *              while (i < *l) {<br>
     *                      *(a + (i*8)) = __initialize(l + 8, len - 1);<br>
     *                      i = i + 1;<br>
     *              }<br>
     *              return a;<br>
     * }
     */

    /**
     * The constructor for creating a multidimensional array
     *
     * Example Translation: new Object[3][2][5]  ==> new Array({3,2,5})
     */
    public Array(int[] lengths) {
        this(lengths[0]);

        if (lengths.length == 1) {
            return;
        }

        int[] newLengths = new int[lengths.length - 1];
        for (int i = 0; i < newLengths.length; i++) {
            newLengths[i] = lengths[i + 1];
        }

        for (int i = 0; i < length; i++) {
            contents[i] = new Array(newLengths);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return Arrays.deepToString(contents);
    }

}
