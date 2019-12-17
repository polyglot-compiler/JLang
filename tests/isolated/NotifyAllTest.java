import java.util.Arrays;

public class NotifyAllTest {

    final static int TOTAL_CLIENTS = 9;
    final static Object lock = new Object();
    static boolean runServer = true;
    static boolean[] finishedClients = new boolean[TOTAL_CLIENTS];
    static int i = 0;

    static boolean allFinished() {
        boolean ans = true;
        for (boolean b : finishedClients) {
            ans &= b;
        }
        return ans;
    }

    static class Server extends Thread {
        @Override
        public void run() {
            for (int j = 0; j < 10; j++) {
                synchronized (lock) {
                    while (!runServer) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Server prints " + i);
                    i++;
                    runServer = !runServer;
                    Arrays.fill(finishedClients, false);
                    lock.notifyAll();
                }
            }
        }
    }

    static class Client extends Thread {

        private int id;

        Client(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            for (int j = 0; j < 10; j++) {
                synchronized (lock) {
                    while (runServer || finishedClients[id]) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Clients print " + i);
                    i++;
                    finishedClients[id] = true;
                    if (allFinished()) {
                        lock.notifyAll();
                        runServer = !runServer;
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server s = new Server();
        s.start();
        for (int j = 0; j < TOTAL_CLIENTS; j++) {
            new Client(j).start();
        }
    }
}
