public class Generic13 {

    static class A<T> {}

    static class B extends A<B> {}

    // Calling f with an argument of raw type A causes
    // T to be inferred as A<T>. Then the upper bound of T
    // becomes A<A<T>> after type variable substitutions.
    // Yet A<T> is not a subtype of A<A<T>>, so the bounds on the
    // type variable are not satisfied.
    static <T extends A<T>> void f(A<T> a) {
        System.out.println(a.getClass().getName());
    }

    public static void main(String[] args) {
        System.out.println("begin");
        A a = new A<B>();
        f(a);
    }
}
