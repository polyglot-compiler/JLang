package basic;

public class Exceptions {
    public static void main(String[] args) {
        simple();

        try {
            tunnel();
        } catch (Throwable e) {
            System.out.println("catch");
        }

        nested();

        throwInFinally();

        System.out.println("end");
    }

    public static void simple() {
        System.out.println("begin simple");
        try {
            throw new Throwable();
        } catch (Throwable e) {
            System.out.println("catch");
        } finally {
            System.out.println("finalize");
        }
    }

    public static void tunnel() {
        System.out.println("begin tunnel");
        try {
            Error err = new Error();
            Exception e = new Exception();
            throw err;
        } catch (Exception e) {
            System.out.println("bad");
        } finally {
            System.out.println("finalize");
        }
    }

    public static void nested() {
        System.out.println("begin nested");
        try {
            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.println("catch inner");
                throw new Error();
            } finally {
                System.out.println("finalize inner");
            }
        } catch (Error e) {
            System.out.println("catch outer");
        } finally {
            System.out.println("finalize outer");
        }
    }

    public static void throwInFinally() {
        System.out.println("begin throwInFinally");
        try {
            try {
                throw new Exception();
            } finally {
                throw new Error();
            }
        } catch (Exception e) {
            System.out.println("caught exception");
        } catch (Error e) {
            System.out.println("caught error");
        }
    }
}
