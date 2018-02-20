public class UnaryOperations {
    public static void main(String[] args){
        int x = -10;
        System.out.println(x);
        x = -x;
        System.out.println(x);

        System.out.println((-100)+ 10);

        System.out.println(+10);
        System.out.println(+(-10));

        System.out.println(~0);
        System.out.println(~10);
        System.out.println(~100);


        int y = 0;
        System.out.println((y++) + (++y));
        System.out.println(y);
        System.out.println((y--) + (--y));
        System.out.println(y);

        System.out.println(!true);
        System.out.println(!false);

    }
}
