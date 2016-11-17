import placeholder.Print;

public class InstanceOf {

    public static void main(String[] args) {
        f(new InstanceOf());
    }

    public static void f(Object o) {
        Print.println(o instanceof InstanceOf);
        Print.println(o instanceof Object);
        Print.println(o instanceof String);
    }
}
