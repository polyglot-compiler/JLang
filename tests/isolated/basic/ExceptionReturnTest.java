public class ExceptionReturnTest {
    public static void main(String[] args) {
        System.out.println(retBoth());
        System.out.println(retCatch());
        System.out.println(retFinally());
        System.out.println(retAfter());
    }

    public static String retBoth(){
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println("C");
            return "DO NOT PRINT";
        } finally {
            System.out.println("F");
            return "CORRECT RETURN -- retBoth";
        }

    }

    public static String retCatch(){
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println("retCatch -- C");
            return "RETURN retCatch";
        } finally {
            System.out.println("retCatch -- F");
        }
    }

    public static String retFinally(){
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println("retFinally -- C");
        } finally {
            System.out.println("retFinally -- F");
            return "RETURN retFinally";
        }
    }

    public static String retAfter(){
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println("retAfter -- C");
        } finally {
            System.out.println("retAfter -- F");
        }
        return "RETURN retAfter";
    }
}
