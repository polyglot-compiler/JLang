public class ArrayOutOfBounds {
    public static void main(String[] args) {
        int[] xs = {0,1,2,3,4};
        try {
            for (int i = 0; i <= xs.length + 10; i++) {
                System.out.println(xs[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("catch");
        }

        try {
            for (int i = 0; i <= xs.length + 10; i++) {
                System.out.println(xs[i] = i+10);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("catch") ;
        }

        int x = 0;
        try {
            System.out.println(xs[--x]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("catch " + x);
        }
    }
}
