/**
 * Created by Daniel on 2/23/17.
 */
public class LabledWhile {
    public static void main(String[] args) {
        boolean x = true;
        outer: while (x){
            System.out.println("inside outer");
            inner: while(x){
                System.out.println("inside inner");
                break outer;
            }
            System.out.println("No");
        }
        System.out.println("Yes");

    }
}
