class Generic10 {

    static class A implements Cloneable {
        @Override
        public A clone() {
            try {
                return (A) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        void f() {
            clone();
        }
    }

    static A[] f() {
        return new A[0];
    }

    public static void main(String[] args) {
        System.out.println("begin");
        new A().clone();
        f().clone();
    }
}
