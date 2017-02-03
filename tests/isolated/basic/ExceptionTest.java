import placeholder.Print;

public class ExceptionTest {
    public static void main(String[] args) {
        try {
            Exception exception = new Exception();
            throw exception;
        } catch (Throwable e) {
            Print.println("e");
        } finally {
            Print.println("f");
        }
    }


    public static void tryBlock() throws Exception {
        Exception exception = new Exception();
        throw exception;
    }

    public static void catchBlock() {
        Print.println("e");
    }

    public static void finallyBlock() throws Exception {
        Print.println("f");
    }


}
