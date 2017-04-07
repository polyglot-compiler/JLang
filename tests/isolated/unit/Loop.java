package unit;

public class Loop {
    public static void main(String[] args) {
        int n = 10;
        for (int i = 0; i < n; ++i) {
            if (i % 2 == 1)
                continue;
            if (i > 0)
                System.out.print(" ");
            System.out.print(i);
        }
        System.out.println();

        int i = 0;
        while (i < n) {
            if (i > 0)
                System.out.print(" ");
            System.out.print(i);
            ++i;
            if (i > n/2) {
                break;
            }
        }
        System.out.println();

        i = n;
        do {
            System.out.println("do-while");
        } while (i < n);

        i = 0;
        do {
            if (i > 0)
                System.out.print(" ");
            System.out.print(i);
            ++i;
        } while (i < n);
        System.out.println();

        boolean x = true;
        outer: while (x) {
            System.out.println("inside outer");
            inner: while (x) {
                System.out.println("inside inner");
                break outer;
            }
            System.out.println("no");
        }
        System.out.println("yes");

        outer2: for (int j = 0; j < n; ++j) {
            System.out.println("inside outer2");
            inner2: for (int k = j + 1; k < n; ++k) {
                System.out.println("inside inner2");
                if (k % 2 == 0) {
                    continue outer2;
                }
            }
            System.out.println("no");
        }
        System.out.println("yes");
    }
}
