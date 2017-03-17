package basic;

public class SimpleClass {
    int field;
    private char field2;

    public SimpleClass(int i) {
        this.field = i;
        this.field2 = (char) i;
    }

    public static void main(String[] args) {
        SimpleClass s = new SimpleClass(0);
        s.field = 12;
        s.field2 = 'A';
        System.out.println(s.method() + s.privateMethod());

        SimpleClassE se = new SimpleClassE(21);
        System.out.println(se.finalMethod() + se.method());

    }

    public int method() {
        privateMethod();
        return field + field2;
    }

    private int privateMethod() {
        field2 = 'A';
        return 23;
    }

    final int finalMethod() {
        field2 = 'B';
        return 74;
    }

    public static class SimpleClassE extends SimpleClass {

        public SimpleClassE(int i) {
            super(i);
        }

        public int method() {
            return super.method() + 1;
        }
    }
}
