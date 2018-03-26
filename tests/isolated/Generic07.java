class Generic07 {

    static class A<T> {
        void f(T t) {}
    }

    static class B<T> {
        private final A<T> a = new A<>();

        void f(T t) {
            a.f(t);
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
    }
}
