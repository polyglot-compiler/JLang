package unit;

public class Array {
    public static void main(String[] args) {
        int[] a = new int[10];
        a[5] = 42;

        print(a);
        for (int i = 0; i < a.length; ++i){
            a[i] = i*2;
        }
        print(a);

        int[][] b = new int[5][5];
        System.out.println(b.length);
        for (int i = 0; i < b.length; ++i)
            System.out.print(b[i].length);
        System.out.println();
        b[1][4] = 42;
        b[4][1] = 42;
        print(b);

        for (int i = 0; i < b.length; ++i)
            for (int j = 0; j < b[i].length; ++j)
                b[i][j] = i * b[i].length + j;
        print(b);
    }

    private static void print(int[] a) {
        for (int i = 0; i < a.length; ++i) {
            if (i > 0)
                System.out.print(" ");
            System.out.print(a[i]);
        }
        System.out.println();
    }

    private static void print(int[][] a) {
        for (int i = 0; i < a.length; ++i) {
            print(a[i]);
        }
    }
}
