public class test {
    public static void main(String[] args) { B.foo(); }
    
    public test() { super(); }
}

class A {
    public static void foo() {
        final java.io.PrintStream flat$$$0 = System.out;
        final byte[] flat$$$1 = new byte[] { 'S', 't', 'a', 't', 'i', 'c', ' ',
        'A', '.', 'f', 'o', 'o' };
        final String flat$$$2 = new String(flat$$$1);
        flat$$$0.println(flat$$$2);
    }
    
    public A() { super(); }
}

class B extends A {
    public B() { super(); }
}
