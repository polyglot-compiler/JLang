public class InheritedStaticMethod extends InheritedStaticMethod_A{
    public static void main(String[] args) {
        InheritedStaticMethod.foo();
    }
}

class InheritedStaticMethod_A{
    public static void foo(){
        System.out.println("Static A.foo");
    }
}