package basic;

public class TryFinally {

    public static void main(String[] args) {
        try {
           throwsExn();
        } catch (Exception e){
            System.out.println("Catch!");
        }

        try {
            nestedTryFinally();
        } catch (Exception e){
            System.out.println("Catch nested try finally!");
        }
    }

    public static void throwsExn() throws Exception{
        try{
            System.out.println("Try block");
            throw new Exception();
        } finally {
            System.out.println("Finally block");
        }
    }

    public static void nestedTryFinally() throws Exception{
        try {
            System.out.println("Try block");
            throw new Exception();
        } finally {
            try {
                System.out.println("Started false");
                throw new Exception();
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
                System.out.println("Ignore");
            }
        }
    }
}
