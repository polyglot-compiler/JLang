class Conditional {

    static boolean t() {
        System.out.println("t");
        return true;
    }

    static boolean f() {
        System.out.println("f");
        return false;
    }

    static int a() {
        System.out.println("a");
        return 1;
    }

    static int b() {
        System.out.println("b");
        return 2;
    }

    public static void main(String[] args) {
        System.out.println("begin");
        System.out.println(true ? 1 : 2);
        System.out.println(false ? 1 : 2);
        System.out.println(t() ? a() : b());
        System.out.println(f() ? a() : b());
    }
}