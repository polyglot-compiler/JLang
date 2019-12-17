public class NotifyTest {

    final static Object lock = new Object();
    static boolean runA = true;
    static int i = 0;

    static class A extends Thread {
        @Override
        public void run() {
            for (int j = 0; j < 10; j++) {
                synchronized (lock) {
                    while (!runA) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("A prints " + i);
                    i++;
                    runA = !runA;
                    lock.notify();
                }
            }
        }
    }

    static class B extends Thread {
        @Override
        public void run() {
            for (int j = 0; j < 10; j++) {
                synchronized (lock) {
                    while (runA) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("B prints " + i);
                    i++;
                    runA = !runA;
                    lock.notify();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        A a = new A();
        B b = new B();
        a.start();
        b.start();
        a.join();
        b.join();
    }
}
