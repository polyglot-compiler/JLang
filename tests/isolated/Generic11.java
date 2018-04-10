class Generic11 {

    interface I<T> {
        void set(T e);
    }

    static class A implements I<String> {
        public void set(String e) {
            System.out.println("setting " + e);
        }
    }

    static class E implements I<String> {
        public void set(Integer n) {
            System.out.println("integer " + n);
        }
        public void set(String s) {
            System.out.println("string " + s);
        }
    }

    static class B<T> {
        void f(T t) {}
    }

    static class C extends B<Integer> {
        void f(Integer n) {
            System.out.println(n);
        }
    }

    static class D extends B<String> {
        void f(Integer n) {
            System.out.println("integer " + n);
        }
        void f(String s) {
            System.out.println("string " + s);
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
        new A().set("hello");
        new C().f(42);
        new D().f(43);
        new D().f("d");
        ((B) new D()).f("cast to raw");
        new E().set(44);
        new E().set("e");
    }
}
