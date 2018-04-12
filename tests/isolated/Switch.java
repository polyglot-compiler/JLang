public class Switch {
    private static final int three = 3;

    public static void main(String[] args) {
        int n = 6;
        label:
        for (int i = 0; i < n; ++i) {
            switch (i) {
                case 0:
                    System.out.println(0);
                    continue label;
                case 1:
                case 2:
                    System.out.println(2);
                    break;
                default:
                    System.out.println("default");
                case 5:
                    System.out.println(5);
                    break;
                case 4:
            }
        }
        System.out.println("after");

        // Non-integer.
        byte b = 2;
        switch (b) {
            case 1:
                System.out.println(1);
                break;
            case 2:
                System.out.println(2);
                break;
            default:
        }

        // Unboxing.
        for (Integer wrapped = 0; wrapped <= 2; ++wrapped) {
            switch (wrapped) {
                case 1:
                    System.out.println(1);
                    break;
                case 2:
                    System.out.println(2);
                    break;
                default:
                    System.out.println("default");
                    break;
            }
        }

        // Expressions.
        final int four = 4;
        for (int i = -1; i <= 2; ++i) {
            switch (i) {
                case -1:
                    System.out.println(-1);
                    break;
                case (byte) (int) 1:
                    System.out.println(1);
                    break;
                case 1+1:
                    System.out.println(2);
                    break;
                case three:
                    System.out.println(3);
                    break;
                case four:
                    System.out.println(4);
                    break;
                default:
                    System.out.println("default");
                    break;
            }
        }

        // Switch on string.
        String s = "hello";
        switch (s) {
            case "no":
                System.out.println("no");
                break;
            case "hello":
                System.out.println("hello");
            case "fallthrough":
                System.out.println("fallthrough");
                break;
            default:
                System.out.println("default");
        }

        // Regression test.
        switch (0) {
            default: if (true);
        }
    }
}
