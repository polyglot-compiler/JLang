import java.lang.Math;
import java.lang.Thread;

public class ThreadingTest extends Thread {

  public void run() { 
    try { 
      // Displaying the thread that is running 
      System.out.println ("Thread " + 
            Thread.currentThread().getId() + 
            " is running"); 
  
    }
    catch (Exception e) {
        // Throwing an exception 
        System.out.println ("Exception is caught"); 
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println("hi");
    System.out.println(Math.random() < 1);

    int n = 8; // Number of threads 
    for (int i=0; i<8; i++) { 
        ThreadingTest object = new ThreadingTest(); 
        object.start();
    }
  }
}