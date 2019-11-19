import java.util.Arrays;

public class Eratosthenes {
    static boolean[] isPrime;
    final static Object lock = new Object();
    static int index = 2;

    public static class Sieve extends Thread {
        private int n;

        Sieve(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            while (true) {
                int start;
                synchronized (lock) {
                    while(index * index <= n && !isPrime[index]) {
                        index++;
                    }
                    if (index * index > n) {
                        return;
                    }
                    start = index;
                    index++;
                }
                for (int i = start; i * start <= n; i++) {
                    isPrime[i * start] = false;
                }
            }
        }
    }

    static int findNumberOfPrimes(int n, int numberOfThreads) {
        isPrime = new boolean[n+1];
        Sieve[] sieves = new Sieve[numberOfThreads];
        Arrays.fill(isPrime, true);
        for (int t = 0; t < numberOfThreads; t++) {
            Sieve sieve = new Sieve(n);
            sieves[t] = sieve;
            sieve.start();
        }

        for (int t = 0; t < numberOfThreads; t++) {
            try {
                sieves[t].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int num = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) {
                num++;
            }
        }
        return num;
    }

    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        int n = Integer.parseInt(args[0]);
        if (args.length == 2) {
            cores = Integer.parseInt(args[1]);
        }
        long start = System.currentTimeMillis();
        int num = findNumberOfPrimes(n, cores);
        long end = System.currentTimeMillis();
        System.out.format("Found %d primes in %d milliseconds", num, end - start);
    }
}
