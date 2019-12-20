public class InterruptTest {
    public static void main(String args[]) {
        Thread current = Thread.currentThread();
        System.out.println(current.isInterrupted());
        current.interrupt();
        System.out.println(current.isInterrupted());
        System.out.println(current.isInterrupted());
        System.out.println(Thread.interrupted());
        System.out.println(current.isInterrupted());
        current.interrupt();
        synchronized (current) {
            try {
                current.wait(1);
                System.out.println("no");
            } catch (InterruptedException e) {
                System.out.println(e.getClass());
            }
        }
        synchronized (current) {
            try {
                current.wait(1);
                System.out.println("yes");
            } catch (InterruptedException e) {
                System.out.println(e.getClass());
            }
        }
        current.interrupt();
        synchronized (current) {
            try {
                current.join();
                System.out.println("yes");
            } catch (InterruptedException e) {
                System.out.println(e.getClass());
            }
        }
    }
}
