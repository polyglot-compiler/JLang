public class StaticMethods {
    public static void main(String[] args) {
        A.foo();
        B.foo();
        C.foo();
        A objA = new C();
        objA.foo();
        B objB = new C();
        objB.foo();
        C objC = new C();
        objC.foo();
    }
    
    public StaticMethods() { super(); }
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

class C extends B {
    public static void foo() {
        final java.io.PrintStream flat$$$3 = System.out;
        final byte[] flat$$$4 = new byte[] { 'S', 't', 'a', 't', 'i', 'c', ' ',
        'C', '.', 'f', 'o', 'o' };
        final String flat$$$5 = new String(flat$$$4);
        flat$$$3.println(flat$$$5);
    }
    
    public C() { super(); }
}
