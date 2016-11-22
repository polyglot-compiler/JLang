import placeholder.Print;

public class ExceptionTest {
    public static void main(String[] args) {
        try {
            Exception exception = new Exception();
            throw exception;
        } catch (Throwable e) {
            Print.println("Correctly caught exception");
        } finally {
            Print.println("Correctly in finally");
        }
    }
}
