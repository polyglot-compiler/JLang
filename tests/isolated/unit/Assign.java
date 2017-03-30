package unit;

public class Assign {
    public static void main(String[] args) {
        int n = 10;
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
    }

    private static void show(int n) {
        System.out.print(n);
        System.out.print(" ");
    }
}
