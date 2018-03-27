class Generic08 {

    interface I<E> {
        void set(E e);
    }

    static class A<E> implements I<E> {
        public void set(E e) {
            System.out.println("setting " + e);
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
        new A<Integer>().set(42);
    }
}
