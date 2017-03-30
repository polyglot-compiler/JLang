package basic;

public class GenericRemoved {
    public static void main(java.lang.String[] args) {
        java.lang.System.out.println(
            new GenericRemoved((java.lang.Object) "Hi There!").toString());
        java.lang.System.out.println(
            new GenericRemoved((java.lang.Object) "Hi There!").toString());
        java.lang.System.out.println(
            new GenericRemoved(new GenericRemoved.A()).toString());
    }

    java.lang.Object obj;

    public GenericRemoved(java.lang.Object obj) {
        super();
        this.obj = obj;
    }

    public java.lang.String toString() {
        return "Generic[".concat(((java.lang.Object) obj).toString()).concat("]");
    }

    private static class A {
        public java.lang.String toString() { return "A"; }
        public A() { super(); }
    }
}