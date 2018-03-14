class Cast {

    static Object f() {
        return "Hello, world!";
    }

    static String g() {
        return (String) new Object();
    }

    static class Inner<T> {
        T field;
        Inner(T field) {
            this.field = field;
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");

        String s = (String) f();
        System.out.println(s);

        try { g(); }
        catch (ClassCastException e) {
            System.out.println("catch g");
        }

        Inner<Integer> i = new Inner<Integer>(new Integer(42));
        System.out.println(i.field);
        System.out.println((Integer) i.field);
        try {
            System.out.println((String) (Object) i.field);
        } catch (ClassCastException e) {
            System.out.println("catch field");
        }

        System.out.println((Integer) 42);
    }
}
