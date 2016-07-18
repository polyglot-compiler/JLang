public class SimpleClass {

    int field;

    public SimpleClass() {
//        field = 4;
    }

    public static void main() {
//        SimpleClass s = new SimpleClass();
//        print(s.method());
    }

    public int method() {
        return 3; // field;
    }

    private int privateMethod() {
        return 23;//(int) new IntLit_c(null, null, 23).value();
    }

    public static native void print(int i);

}
