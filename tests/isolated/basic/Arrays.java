package basic;

public class Arrays {
    public static void main(String[] args){
        int[] a = new int[10];
        a[5] = 9999999;

        for (int i=0; i<a.length; i++){
            System.out.println(a[i]);
            a[i] = i*2;
        }

        for (int i=0; i<a.length; i++){
            System.out.println(a[i]);
        }

        a = new int[]{0, 1, 2, 3, 4};

        for (int i=0; i<a.length; i++){
            System.out.println(a[i]);
        }

        int[][] b = new int[5][5];
        System.out.println(b.length);
        System.out.println(b[1].length);
        b[1][4] = 42;
        b[4][1] = 42;

        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                System.out.println(b[i][j]);
                b[i][j] = i * b[i].length + j;
            }
        }

        for (int i = 0; i < b.length; i++)
            for (int j = 0; j < b[i].length; j++)
                System.out.println(b[i][j]);

        b = new int[][] {{1,2},{3,4}};

        for (int i = 0; i < b.length; i++)
            for (int j = 0; j < b[i].length; j++)
                System.out.println(b[i][j]);
    }
}
