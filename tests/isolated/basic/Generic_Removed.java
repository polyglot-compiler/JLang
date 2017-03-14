public class Generic_Removed {
    public static void main(java.lang.String[] args) {
        java.lang.System.out.println(new Generic_Removed((java.lang.Object)
                "Hi There!").toString());
        java.lang.System.out.println(new Generic_Removed((java.lang.Object)
                "Hi There!").toString());
        java.lang.System.out.println(new Generic_Removed(new Generic_Removed.A()).toString());
    }

    java.lang.Object obj;

    public Generic_Removed(java.lang.Object obj) {
        super();
        this.obj = obj;
    }

    public java.lang.String toString() {
        return "Generic[".concat(((java.lang.Object) obj).toString()).concat(
                "]");
    }

    private static class A {
        public java.lang.String toString() { return "A"; }

        public A() { super(); }
    }

}