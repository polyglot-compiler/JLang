public class Subtype {

    public static void main(String[] args) {
        A a = new A().getValue$$();
        B b = new B().getValue$$();

        System.out.println(a + " -- " + b);
    }

    private static class A {

        public A getValue$$() {
            return new A();
        }

        @Override
        public String toString() {
            return "A";
        }
    }

    private static class B extends A{
        @Override
        public B getValue$$() {
            return new B();
        }

        @Override
        public String toString() {
            return "B";
        }
    }

}
