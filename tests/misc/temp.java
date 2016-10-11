public class temp {
    public static void main(String[] args) {
        String x;
        x = "" + 4 + 3;
        System.out.println(x);
        x = "" + f();
        System.out.println(x);
        x = "woah" + null;
        System.out.println(x);
        x = "woah" + new Object();
        System.out.println(x);
    }

    private static int f(){
        return 1;
    }
}
