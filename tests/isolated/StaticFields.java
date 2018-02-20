public class StaticFields {
    private static int i = 1;
    private static int j = 2;
    private static int k;

    public static void main(String[] args) {
        System.out.println(i);
        System.out.println(j);
        k = i + j;
        System.out.println(k);
    }
}
