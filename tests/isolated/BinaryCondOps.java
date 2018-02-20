public class BinaryCondOps {
    public static void main(String[] args) {
        boolean x = false && printMsgTrue();
        assertBool(!x);
        x = false && printMsgFalse();
        assertBool(!x);

        x = true && printMsgTrue();
        assertBool(x);
        x = true && printMsgFalse();
        assertBool(!x);

        x = false || printMsgTrue();
        assertBool(x);
        x = false || printMsgFalse();
        assertBool(!x);

        x = true || printMsgTrue();
        assertBool(x);
        x = true || printMsgFalse();
        assertBool(x);
    }

    static void assertBool(boolean x){
        if (x){
            System.out.println("GOOD");
        } else {
            System.out.println("FAIL");
        }
    }


    static boolean printMsgTrue(){
        System.out.println("MSG");
        return true;
    }

    static boolean printMsgFalse(){
        System.out.println("MSG");
        return false;
    }
}
