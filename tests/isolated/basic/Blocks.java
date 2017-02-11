import placeholder.Print;

/**
 * Created by Daniel on 2/11/17.
 */
public class Blocks {
    public static void main(String[] args) {
        {
            Print.print(1);
        }
        {
            Print.print(2);
        }
        {
            {
                Print.print(3);
            }
            {
                Print.print(4);

            }
        }
    }

}
