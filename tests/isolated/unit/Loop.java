package unit;

public class Loop {
    public static void main(String[] args) {
        int n = 10;
        for (int i = 0; i < n; ++i) {
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
    }
}
