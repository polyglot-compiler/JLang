/**
 * Created by Daniel on 3/14/17.
 */
public class InnerClass {
    public static void main(String[] args) {
        System.out.println(new Inner().toString());
    }

    private static class Inner {
        @Override
        public String toString() {
            return "Inner";
        }
    }
}
