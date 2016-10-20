import placeholder.Print;

public class StaticFields {
    private static int i = 1;
    private static int j = 2;
    private static int k;

    public static void main(String[] args) {
        Print.println(i);
        Print.println(j);
        k = i + j;
        Print.println(k);
    }
}
