import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadingTest extends Thread {

    private int id;
    private final static List<String> output = new ArrayList<>();

    public ThreadingTest(int id) {
        this.id = id;
    }

    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                // Displaying the thread that is running
                synchronized (output) {
                    output.add("Thread " + id + " is running");
                }
            }
        } catch (Exception e) {
            // Throwing an exception
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("hi");
        System.out.println(Math.random() < 1);

        int n = 8; // Number of threads
        ThreadingTest[] pool = new ThreadingTest[8];
        for (int i = 0; i < 8; i++) {
            ThreadingTest object = new ThreadingTest(i);
            pool[i] = object;
            object.start();
        }
        for (ThreadingTest t : pool) {
            t.join();
        }

        Collections.sort(output);
        for (String s : output) {
            System.out.println(s);
        }
        System.out.println(output.size());
    }
}