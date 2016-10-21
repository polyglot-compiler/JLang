import placeholder.Print;

public class SimpleClass {
    int field;
    private char field2;

    public SimpleClass() {}

    public static void main(String[] args) {
        SimpleClass s = new SimpleClass();
        s.field = 12;
        s.field2 = 'A';
        Print.println(s.method() + s.privateMethod());
    }

    public int method() {
        privateMethod();
        return field + field2;
    }

    private int privateMethod() {
        field2 = 'A';
        return 23;
    }
}
