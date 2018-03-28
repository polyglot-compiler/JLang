class Initializer {
    int i = 42;

    static { System.out.println("Static"); }
    { System.out.println(i); }
    { System.out.println("After"); }

    Initializer(int i) {
        System.out.println("Inner constructor");
        System.out.println(i);
    }

    Initializer(String s) {}

    Initializer() {
        this(27);
        System.out.println("Outer constructor");
    }

    public static void main(String[] args) {
        Initializer i = new Initializer();
        i = new Initializer();
        i = new Subclass();
        i = new Subclass();
    }

    static class Subclass extends Initializer {
        int j = 24;

        { System.out.print("Subclass "); System.out.println(j); }
        { System.out.println("Subclass after"); }

        Subclass(int n) {
            super();
            System.out.println("Subclass inner constructor");
        }

        Subclass() {
            this(0);
            System.out.println("Subclass outer constructor");
        }
    }
}

