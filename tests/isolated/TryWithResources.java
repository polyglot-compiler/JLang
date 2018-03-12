class TryWithResources {

    static class Closeable implements AutoCloseable {
        private final String arg;

        Closeable(String arg) {
            this.arg = arg;
        }

        @Override
        public void close() {
            System.out.println("closing " + this);
        }

        @Override
        public String toString() {
            return arg;
        }
    }

    static class ThrowInClose implements AutoCloseable {
        @Override
        public void close() {
            System.out.println("throwing runtime exception");
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");

        try (Closeable c = new Closeable("single simple")) {
            System.out.println("try single simple");
            System.out.println(c);
        }

        try (Closeable c = new Closeable("single finally")) {
            System.out.println("try single finally");
            System.out.println(c);
        }
        finally {
            System.out.println("finally");
        }

        try (Closeable c = new Closeable("single catch")) {
            System.out.println("try single catch");
            System.out.println(c);
        }
        catch (RuntimeException e) {
            System.out.println("catch");
        }

        try (
            Closeable a = new Closeable("multi simple a");
            Closeable b = new Closeable("multi simple b");
        ) {
            System.out.println("try multi simple");
            System.out.println(a);
            System.out.println(b);
        }

        try (
            Closeable a = new Closeable("multi complex a");
            Closeable b = new Closeable("multi complex b");
        ) {
            System.out.println("try multi complex");
            System.out.println(a);
            System.out.println(b);
            try (Closeable c = new Closeable("single nested")) {
                System.out.println("try single nested");
                System.out.println(c);
            }
            throw new RuntimeException();
        }
        catch (RuntimeException e) {
            System.out.println("catch");
        }
        finally {
            System.out.println("finally");
        }

        try (
            ThrowInClose c = new ThrowInClose();
        ) {
            System.out.println("try throw in close");
        }
        catch (RuntimeException e) {
            System.out.println("catch");
        }
        finally {
            System.out.println("finally");
        }

        System.out.println("end");
    }
}
