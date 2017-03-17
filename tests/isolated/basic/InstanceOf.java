package basic;

public class InstanceOf implements IA {

    public static void main(String[] args) {
        f(new InstanceOf());
    }

    public static void f(Object o) {
        System.out.println(o instanceof InstanceOf);
        System.out.println(o instanceof Object);
        System.out.println(o instanceof String);
        System.out.println(o instanceof IA);
        System.out.println(o instanceof IB);
    }
}

interface IA {}
interface IB {}
