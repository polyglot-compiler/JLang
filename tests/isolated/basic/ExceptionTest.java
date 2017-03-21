package basic;

public class ExceptionTest {
    public static void main(String[] args) {
        try {
            Throwable exception = new Throwable();
            throw exception;
        } catch (Throwable e) {
            System.out.println("Correctly Catch");
        } finally {
            System.out.println("Correctly Finalize");
        }

        try {
            f();
        } catch (Throwable e){
            System.out.println("Correctly Catch");
        }
    }

    public static void f(){
        try {
            Error exception = new Error();
            Exception e = new Exception();
            throw exception;
        } catch (Exception e) {
            System.out.println("DO NOT CATCH THIS");
        } finally {
            System.out.println("Correctly Finalize");
        }
    }
}
