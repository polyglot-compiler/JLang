package java.lang;

public class Object {

    protected Object clone() throws CloneNotSupportedException {
        // TODO
        return this;
    }

    public boolean equals(Object other) {
        // TODO
        return false;
    }

    // TODO: The finalize method is not yet supported.
    private final void finalize() throws Throwable {}

    public final native Class getClass();

    public int hashCode() {
        // TODO
        return 0;
    }

    public final void notify() {
        // TODO
    }

    public final void notifyAll() {
        // TODO
    }

    public String toString() {
        // TODO
        return getClass().toString();
    }

    public final void wait(long x) throws InterruptedException {
        // TODO
    }

    public final void wait(long x, int y) throws InterruptedException {
        // TODO
    }

    public final void wait() throws InterruptedException {
        // TODO
    }
}
