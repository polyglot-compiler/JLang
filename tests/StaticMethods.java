public class StaticMethods {
    public static void main(String[] args) {
        A.foo();
//        B.foo();
        C.foo();

        A objA = new C();
//        objA.foo();

        B objB = new C();
//        objB.foo();

        C objC = new C();
//        objC.foo();
    }
}

class A {
    public static void foo() {
        System.out.println("Static A.foo");
    }
}

class B extends A {

}

class C extends B {
    public static void foo() {
        System.out.println("Static C.foo");
    }
}
