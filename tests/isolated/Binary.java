class Binary {

    static boolean f() {
        System.out.println("f");
        return true;
    }

    static boolean g() {
        System.out.println("g");
        return false;
    }

    public static void main(String[] args) {
        System.out.println(1 + 2 + 3);
        System.out.println(1.0 + 2.1 == 3.1);
        System.out.println(33 << 3);
        System.out.println(33 >> 3L);
        System.out.println(-33L >>> 3);
        System.out.println(33 ^ 23);
        System.out.println(23 | 47);
        System.out.println(7 & 32);

        System.out.println(3  - 2);
        System.out.println(33 / 3);
        System.out.println(33 % 15);
        System.out.println(33 * 15);

        System.out.println(3.0  - 2.1  == 0.9);
        System.out.println(33.0 / 3.0  == 11.0);
        System.out.println(33.0 * 15.0 == 495.0);

        System.out.println(3 <  3);
        System.out.println(3 <= 3);
        System.out.println(3 == 3);
        System.out.println(3 != 3);
        System.out.println(3 >  3);
        System.out.println(3 >= 3);

        System.out.println(3 <  4);
        System.out.println(3 <= 4);
        System.out.println(3 == 4);
        System.out.println(3 != 4);
        System.out.println(3 >  4);
        System.out.println(3 >= 4);

        System.out.println(3 <  2);
        System.out.println(3 <= 2);
        System.out.println(3 == 2);
        System.out.println(3 != 2);
        System.out.println(3 >  2);
        System.out.println(3 >= 2);

        System.out.println(3.0 <  3.0);
        System.out.println(3.0 <= 3.0);
        System.out.println(3.0 == 3.0);
        System.out.println(3.0 != 3.0);
        System.out.println(3.0 >  3.0);
        System.out.println(3.0 >= 3.0);

        System.out.println(3.0 <  4.0);
        System.out.println(3.0 <= 4.0);
        System.out.println(3.0 == 4.0);
        System.out.println(3.0 != 4.0);
        System.out.println(3.0 >  4.0);
        System.out.println(3.0 >= 4.0);

        System.out.println(3.0 <  2.0);
        System.out.println(3.0 <= 2.0);
        System.out.println(3.0 == 2.0);
        System.out.println(3.0 != 2.0);
        System.out.println(3.0 >  2.0);
        System.out.println(3.0 >= 2.0);

        System.out.println(f() && g());
        System.out.println(g() && f());
        System.out.println(f() || g());
        System.out.println(g() || f());

        System.out.println("hello" + " " + "world");

        // Regression test.
        System.out.println(true & false);
        System.out.println(true & true);
    }
}
