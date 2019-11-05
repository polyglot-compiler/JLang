public class Synchronized {
    public static void main(String[] args) {
        Synchronized obj = new Synchronized();
        synchronized (obj) {
            System.out.println("Hello");
        }
        try {
            testtry();
        } catch (Exception e) {
            System.out.println("caught e: " + e.getMessage());
        }

        try {
            synchronized (obj) {
                throw new Exception("inner throw");
            }
        } catch (Exception e) {
            System.out.println("caught e: " + e.getMessage());
        }

        synchronized (obj) {
            System.out.println("Acquired lock after exceptions");
            obj.doStuff();
        }
    }

    public static synchronized void testtry() throws Exception {
        System.out.println("entertest");
        throw new Exception("message1");
    }

    public synchronized void doStuff() {
        System.out.println("acquired owned lock succeeded");
    }

    public static void synchronized_enter(Object o) {
        //checking that the desugar name for monitor enter isn't used as
        //special constant anywhere. This method should never be called
        System.out.println("yesss");
        return;
    }

}