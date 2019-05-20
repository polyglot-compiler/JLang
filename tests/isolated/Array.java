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

        methodAndFieldTest();
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

    public static void testObject(Object o) {
        System.out.println(o.getClass());
        String s = o.toString();
        System.out.println(o.equals(o));
        int h = o.hashCode();
    }

    public static void testNewArray() {
        int[][] x = new int[1][1];
        testObject(x);
        testObject(x[0]);
        testObject(x.clone());
        testObject(x[0].clone());
    }

    private static void methodAndFieldTest() {
        testNewArray();
        System.out.println(int[].class.getName());
        System.out.println(new int[1].getClass().getName());
        System.out.println(new int[]{1}.getClass().getName());
        System.out.println(new int[1][].getClass().getName());
        System.out.println(new String[1].getClass().getName());
        System.out.println(new int[][]{{1, 1}, {1, 1}}.getClass().getName());
        int[] y = new int[3];
        System.out.println(y.length);
        System.out.println(y.clone().length);
        y[1] = 12345;
        System.out.println(y.clone()[1]);
        System.out.println(y.equals(y));
        System.out.println(y.equals(null));
        System.out.println(y.getClass());
        System.out.println(y[0]);
        int h = y.hashCode();
        int[][] x = new int[1][1];
        System.out.println(x.getClass());
        String s = x.toString();
        System.out.println(x.getClass().getName());
        System.out.println(x[0].getClass().getName());
    }
}
