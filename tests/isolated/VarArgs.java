class VarArgs {

    VarArgs(int... ns) {
        System.out.println("constructor");
        System.out.println(ns.length);
    }

    public static void main(String[] args) {
        System.out.println("begin");
        f();
        f("hello");
        f("hello", "world");
        f(3, "repeat", "thrice");
        g((short) 1, (byte) 2, 3);
        new VarArgs(1, 2, 3);
    }

    static void f(String s) {
        System.out.println("f " + s);
    }

    static void f(String... ss) {
        System.out.print("f");
        for (int i = 0; i < ss.length; ++i) {
            System.out.print(" " + ss[i]);
        }
        System.out.println();
    }

    static <T extends String> void f(int repeat, T... ss) {
        while (repeat --> 0) {
            f(ss);
        }
    }

    static void g(int... ns) {
        System.out.print("g");
        for (int i = 0; i < ns.length; ++i) {
            System.out.print(" " + ns[i]);
        }
        System.out.println();
    }
}
