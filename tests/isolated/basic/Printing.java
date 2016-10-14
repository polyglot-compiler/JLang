// FIXME: if this line is removed, polyllmvc will just
//        assume that Print comes from java.lang, resulting
//        in a linking error later on.
import placeholder.Print;

public class Printing {
    public static void main() {
        Print.println(1);
        Print.println(2);
        Print.println(3);
    }
}
