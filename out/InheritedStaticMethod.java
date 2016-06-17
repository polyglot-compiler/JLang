public class InheritedStaticMethod {
    public static void main(String[] args) { InheritedStaticMethod_B.foo(); }
    
    public InheritedStaticMethod() { super(); }
}

class InheritedStaticMethod_A {
    public static void foo() {
        final java.io.PrintStream flat$$$0 = System.out;
        final byte[] flat$$$1 = new byte[] { 'S', 't', 'a', 't', 'i', 'c', ' ',
        'A', '.', 'f', 'o', 'o' };
        final String flat$$$2 = new String(flat$$$1);
        flat$$$0.println(flat$$$2);
    }
    
    public InheritedStaticMethod_A() { super(); }
}

class InheritedStaticMethod_B extends InheritedStaticMethod_A {
    public InheritedStaticMethod_B() { super(); }
}
