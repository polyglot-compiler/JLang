public class Add {
    public static void main(String[] args) {
        // String s = args[1];
        f(1);
    }

    public static int f(int i) {
        char c = 1;
        short s = 2;
        if (i < 2 && i > -10) {
            if (i < 3) {
                return 3;
            } else {
                return 4;
            }
        }
        i += i;
        i = (int) (1 + 2l);
        long j = i;
        return c + s;
    }

    public static native int g();
}
