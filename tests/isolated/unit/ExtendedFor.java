package unit;

import java.util.Iterator;

class ExtendedFor {

    public static void main(String[] args) {
        System.out.println("begin");
        int[] xs = new int[] {};
        print(xs);
        xs = new int[] {1};
        print(xs);
        xs = new int[] {1, 2};
        print(xs);
        xs = new int[] {1, 2, 3, 4, 5};
        print(xs);

        for (String greeting : new Greeter()) {
            System.out.println(greeting);
        }
    }

    private static void print(int[] xs) {
        System.out.print("xs");
        for (int x : xs)
            System.out.print(" " + x);
        System.out.println();
    }

    private static class GreeterIterator implements Iterator<String> {
        private int count = 0;

        public boolean hasNext() {
            return count < 5;
        }

        public String next() {
            return count < 5 ? "Hello " + ++count : null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class Greeter implements Iterable<String> {
        public Iterator<String> iterator() {
            return new GreeterIterator();
        }
    }
}
