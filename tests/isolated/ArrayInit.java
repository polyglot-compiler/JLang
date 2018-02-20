public class ArrayInit {
    public static void main(String[] args) {
        int[] a = new int[] {1, 2, 3, 4};
        print(a);

        int x = 1;
        int[][] b = new int[][] {{x++, x++, x++},
                                 {x++, x++, x++},
                                 {x++, x++, x++}};
        print(b);

        int[][][][] c = new int[][][][] {{{{42}}}};
        System.out.println(c[0][0][0][0]);

        int[][] d = new int[][] {null, null, null};
        print(d);

        byte[] e = new byte[] {1, 2, 3, 4};
        print(e);

        char[] f = new char[] {'a', 'b', 'c', 'd'};
        print(f);

        short[] g = new short[] {1, 2, 3, 4};
        print(g);

        long[] h = new long[] {1, 2, 3, 9223372036854775807L};
        print(h);
    }

    private static void print(int[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(byte[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(char[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(short[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(long[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(int[][] a) {
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == null) {
                System.out.println("null");
            } else {
                print(a[i]);
            }
        }
    }
}
