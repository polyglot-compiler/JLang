public class Inc {
    private static int f() {
        System.out.println("Function called");
        return 0;
    }

    public static void main(String[] args) {
        int a[] = new int[1];
        System.out.println(a[0]);
        a[f()] += 1;
        System.out.println(a[0]);
        a[f()] *= 4;
        System.out.println(a[0]);
        a[f()] -= 1;
        System.out.println(a[0]);
        a[f()] /= 2;
        System.out.println(a[0]);
        a[f()] <<= 1;
        System.out.println(a[0]);
    }
}
