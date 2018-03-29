class Generic09 {

    interface I {
        <T> T[] toArray(T[] a);
    }

    static class A implements I {
        public <T> T[] toArray(T[] a) {
            return a;
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
        I a = new A();
        System.out.println(a.toArray(new String[42]).length);
    }
}