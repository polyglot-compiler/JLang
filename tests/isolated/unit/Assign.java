package unit;

public class Assign {
    static int g = ten();
    int i = ten();

    static int ten() {
        System.out.println("Side effect");
        return 10;
    }

    Assign(int n) {}

    Assign() {
        // Test that member initializer side effects are not duplicated.
        this(0);
    }

    public static void main(String[] args) {
        int n = ten();
        show(n);
        n += 3;
        show(n);
        n -= 3;
        show(n);
        n *= 2;
        show(n);
        n /= 2;
        show(n);
        n %= 6;
        show(n);
        n |= 0x12345678;
        show(n);
        n &= 0x87654321;
        show(n);
        n >>= 3;
        show(n);
        n <<= 2;
        show(n);
        n = -1;
        n >>>= 12;
        show(n);
        System.out.println();

        show(g);
        g += 3;
        show(g);
        g -= 3;
        show(g);
        g *= 2;
        show(g);
        g /= 2;
        show(g);
        g %= 6;
        show(g);
        g |= 0x12345678;
        show(g);
        g &= 0x87654321;
        show(g);
        g >>= 3;
        show(g);
        g <<= 2;
        show(g);
        g = -1;
        g >>>= 12;
        show(g);
        System.out.println();

        Assign a = new Assign();
        show(a.i);
        a.i += 3;
        show(a.i);
        a.i -= 3;
        show(a.i);
        a.i *= 2;
        show(a.i);
        a.i /= 2;
        show(a.i);
        a.i %= 6;
        show(a.i);
        a.i |= 0x12345678;
        show(a.i);
        a.i &= 0x87654321;
        show(a.i);
        a.i >>= 3;
        show(a.i);
        a.i <<= 2;
        show(a.i);
        a.i = -1;
        a.i >>>= 12;
        show(a.i);
        System.out.println();
    }

    static void show(int n) {
        System.out.print(n);
        System.out.print(" ");
    }
}
