/**
 * Created by Daniel on 3/9/17.
 */
public class ArrayOutOfBounds {
    public static void main(String[] args) {
        int[] xs = {0,1,2,3,4};
        try {
            for (int i = 0; i <= xs.length + 10; i++) {
                System.out.println(xs[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Correctly caught");
        }

        try {
            for (int i = 0; i <= xs.length + 10; i++) {
                System.out.println(xs[i] = i+10);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Correctly caught");
        }
    }
}
