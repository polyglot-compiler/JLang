class Annotations {

    enum T {T1, T2}

    @Target(T.T1)
    public @interface Target {
        T[] value();
    }

    @Target({T.T1, T.T2})
    public @interface A {}

    @A
    void f() {}

    public static void main(String[] arg) {
        System.out.println("begin");
    }
}
