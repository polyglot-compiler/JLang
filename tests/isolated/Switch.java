public class Switch {
    public static void main(String[] args) {
        int n = 6;
        label:
        for (int i = 0; i < n; ++i) {
            switch (i) {
                case 0:
                    System.out.println("first");
                    continue label;
                case 1:
                case 2:
                    System.out.println("second");
                    break;
                default:
                    System.out.println("default");
                case 5:
                    System.out.println("third");
                    break;
                case 4:
            }
        }
        System.out.println("after");

        byte b = 2;
        switch (b) {
            case 1:
                System.out.println("first");
                break;
            case 2:
                System.out.println("second");
                break;
            default:
        }

        // TODO: Polyglot doesn't support switches on Strings yet.
        // String s = "hello";
        // switch (s) {
        //     case "nope":
        //         System.out.println("nope");
        //         break;
        //     case "hello":
        //         System.out.println("hello");
        //         break;
        //     default:
        //         System.out.println("default");
        // }

        // TODO: Enums and primitive wrapper classes.
    }
}
