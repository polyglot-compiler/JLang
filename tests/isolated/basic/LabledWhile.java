import placeholder.Print;

/**
 * Created by Daniel on 2/23/17.
 */
public class LabledWhile {
    public static void main(String[] args) {
        boolean x = true;
        outer: while (x){
            Print.println("inside outer");
            inner: while(x){
                Print.println("inside inner");
                break outer;
            }
            Print.println("No");
        }
        Print.println("Yes");

    }
}
