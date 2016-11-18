import placeholder.Print;

public class Main {

    public static void main(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            Print.println(args[i]);
        }
    }
}
