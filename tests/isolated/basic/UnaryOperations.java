import placeholder.Print;

public class UnaryOperations {
    public static void main(String[] args){
        int x = -10;
        Print.println(x);
        x = -x;
        Print.println(x);

        Print.println((-100)+ 10);

        Print.println(+10);
        Print.println(+(-10));

        Print.println(~0);
        Print.println(~10);
        Print.println(~100);

        println(!true);
        println(!false);

    }

    private static void println(boolean b){
        Print.println(b ? 1 : 0);
    }
}
