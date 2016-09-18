package classes;

public class Array {

    public final int length;
    public final java.lang.Object[] z_contents = null; // prefixed with z so it comes last in list of fields

    private Array(int length) {
        this.length = length;
//        z_contents = null; //new java.lang.Object[length];
    }

    /**
     * The constructor for creating a multidimensional array
     *
     * Example Translation: new Object[3][2][5]  ==> new Array({3,2,5})
     */
    private Array(int[] lengths) {
        length = lengths[0];
//        z_contents = null; // new java.lang.Object[length];

        if (lengths.length == 1) {
            return;
        }

        int[] newLengths = new int[lengths.length - 1];
        for (int i = 0; i < newLengths.length; i++) {
            newLengths[i] = lengths[i + 1];
        }

        for (int i = 0; i < length; i++) {
            //z_contents[i] = new Array(newLengths);
            java.lang.Object TEMPOBJECT = new Array(newLengths);
            int INDEX = i;
        }
    }

    public java.lang.Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
