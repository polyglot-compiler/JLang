import placeholder.Print;

public class InstanceOf implements IA {

    public static void main(String[] args) {
        f(new InstanceOf());
    }

    public static void f(Object o) {
        Print.println(o instanceof InstanceOf);
        Print.println(o instanceof Object);
        Print.println(o instanceof String);
        Print.println(o instanceof IA);
        Print.println(o instanceof IB);
    }
}

interface IA {}
interface IB {}
