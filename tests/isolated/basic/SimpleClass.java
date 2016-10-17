public class SimpleClass {

    int field;
    private char field2;

    public SimpleClass() {
//        field = 4;
    }
//
//    public static void main() {
//        SimpleClass s = new SimpleClass();
//        s.field = 12;
//        s.field2 = 'A';
//        print(s.method() + s.privateMethod());
//    }

    public int method() {
        privateMethod();
        return field + field2; // 3;
    }

    private int privateMethod() {
        field2 = 'A';
        return 23;//(int) new IntLit_c(null, null, 23).value();
    }

    public static native void print(int i);

//    public static void print(int i) {
//        System.out.println(i);
//    }

}
