package support;

public class Array {

    public final int length;
    public final Object[] contents;

    private Array(int length) {
        this.length = length;
        contents = new Object[length];
    }

    /**
     * The constructor for creating a multidimensional array
     *
     * Example Translation: new Object[3][2][5]  ==> new Array({3,2,5})
     */
    private Array(int[] lengths) {
        length = lengths[0];
        contents = new Object[lengths[0]];

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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
