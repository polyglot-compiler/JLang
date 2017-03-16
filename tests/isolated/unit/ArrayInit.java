package unit;

public class ArrayInit {
    public static void main(String[] args) {
        int[] a = new int[] {1, 2, 3, 4};
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();

        int x = 1;
        int[][] b = new int[][] {{x++, x++, x++},
                                 {x++, x++, x++},
                                 {x++, x++, x++}};
        for (int i = 0; i < b.length; ++i) {
            for (int j = 0; j < b[i].length; ++j) {
                System.out.print(b[i][j]);
            }
        }
        System.out.println();

        int[][][][] c = new int[][][][] {{{{42}}}};
        System.out.println(c[0][0][0][0]);
    }
}
