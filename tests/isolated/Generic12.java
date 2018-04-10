class Generic12 {

    public static void main(String[] args) {
        A a = new A<String>(); // A {String / T}
        a.f("A");

        B b = new B();
        b.f("B");
        ((A) b).f("(A) B");
    }

    static class A<T> {
        int i = 0;
        public void f(T t) { System.out.println("T " + t); }
    }

    static class B extends A<String> {
        public void f(String s) { System.out.println("String " + s); }
    }
}
