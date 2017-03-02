public class ExceptionTest {
    public static void main(String[] args) {
        try {
            Exception exception = new Exception();
            throw exception;
        } catch (Throwable e) {
            System.out.println("e");
        } finally {
            System.out.println("f");
        }
    }


    public static void tryBlock() throws Exception {
        Exception exception = new Exception();
        throw exception;
    }

    public static void catchBlock() {
        System.out.println("e");
    }

    public static void finallyBlock() throws Exception {
        System.out.println("f");
    }


}
