import java.util.Iterator;

class EnhancedFor {

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

        label: for (int x : xs) {
            while (true) {
                if (x < 3)
                    continue label;
                if (x > 3)
                    break label;
                System.out.println(x);
                break;
            }
        }

        for (Integer i : new Counter(3)) {
            System.out.println(i);
        }

        for (Object o : new RawCounter()) {
            System.out.println(o);
        }

        for (int i : new Counter(3)) {
            for (int j : new Counter(3)) {
                System.out.println(i + " " + j);
            }
        }

        for (float i : new Counter5()) {
            System.out.println((int) i);
        }
    }

    private static void print(int[] xs) {
        System.out.print("xs");
        for (int x : xs)
            System.out.print(" " + x);
        System.out.println();
    }

    private static class CountIterator implements Iterator<Integer> {
        private int count = 0;
        private final int total;

        public CountIterator(int total) {
            this.total = total;
        }

        public boolean hasNext() {
            return count < total;
        }

        public Integer next() {
            return ++count;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class CountIteratorSubclass extends CountIterator {
        public CountIteratorSubclass(int total) {
            super(total);
        }
    }

    private static class Counter implements Iterable<Integer> {
        private final int total;

        public Counter(int total) {
            this.total = total;
        }

        public Iterator<Integer> iterator() {
            return new CountIteratorSubclass(total);
        }
    }

    private static class Counter5 extends Counter {

        public Counter5() {
            super(5);
        }
    }

    private static class RawIterator implements Iterator {
        private int count = 0;
        private final int total;

        public RawIterator(int total) {
            this.total = total;
        }

        public boolean hasNext() {
            return count < total;
        }

        public Object next() {
            return new Integer(++count);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RawCounter implements Iterable {

        public Iterator iterator() {
            return new RawIterator(3);
        }
    }
}
