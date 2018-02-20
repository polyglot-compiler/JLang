public class Blocks {
    public static void main(String[] args) {
        {
            System.out.print(1);
        }
        {
            System.out.print(2);
        }
        {
            {
                System.out.print(3);
            }
            {
                System.out.print(4);

            }
        }
    }

}
