import placeholder.Print;

public class ExceptionTest {
    public static void main(String[] args) {
        try {
            Throwable exception = new Throwable();
            throw exception;
        } catch (Throwable e) {
            Print.println("Correctly Catch");
        } finally {
            Print.println("Correctly Finalize");
        }

        try {
            f();
        } catch (Throwable e){
            Print.println("Correctly Catch");

        }
    }

    public static void f(){
        try {
            Error exception = new Error();
            Exception e = new Exception();
            throw exception;
        } catch (Exception e) {
            Print.println("DO NOT CATCH THIS");
        } finally {
            Print.println("Correctly Finalize");
        }

    }
}
