class Generic14 {

    static class A<T> {
        class Inner<E> {
            void f() {
                System.out.println("f");
            }
        }

        void createInner() {
            new Inner<T>().f();
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
        new A<String>().createInner();
    }
}
