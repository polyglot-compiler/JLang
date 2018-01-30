package basic;

public class ExceptionTest {
    public static void main(String[] args) {
        simple();

        try {
            tunnel();
        } catch (Throwable e) {
            System.out.println("catch tunnel");
        }

        nested();

        System.out.println("end");
    }

    public static void simple() {
        try {
            throw new Throwable();
        } catch (Throwable e) {
            System.out.println("catch simple");
        } finally {
            System.out.println("finalize simple");
        }
    }

    public static void tunnel() {
        try {
            Error err = new Error();
            Exception e = new Exception();
            throw err;
        } catch (Exception e) {
            System.out.println("bad tunnel");
        } finally {
            System.out.println("finalize tunnel");
        }
    }

    public static void nested() {
        try {
            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.println("catch inner");
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("catch outer");
        }
    }
}
