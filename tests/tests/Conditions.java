package tests;

public class Conditions {
//    public static void main(String[] args) {
//        main();
//    }

    public static void main() {
        int x = 5 + 0;
        boolean y = 1 > 0;//true;
        boolean z = 1 > 0;//true;
        while (x * 2 < 33) {
            if (!y | z) {
                while (z) {
                    x = x - 1;
                    print(1);
                    if (x < 3) {
                        z = false;
                        print(2);
                    }
                }
                x = x * 2;
                print(3);
                print(x);
            }
            x = x * 4;
            print(4);
        }

        print(5);
        print(x);
        int i = 6;
        while (i > 4) {
            i = i - 1;
            x = x * x;
        }
        if (true != false) {
//            while (5 <= 4) {
//                print(100000000);
//            }
            while (x >= 30) {
                x = -600;
                print(7);
            }
        }

    }

    private void f() {
    }

    private void g() {
    }

    public void h() {
    }

    public static native void print(int i);

//    public static void print(int i) {
//        System.out.println(i);
//    }

}
