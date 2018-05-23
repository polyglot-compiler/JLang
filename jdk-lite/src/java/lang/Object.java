package java.lang;

public class Object {

    protected Object clone() throws CloneNotSupportedException {
        return this;
    }

    public boolean equals(Object other) {
        return this == other;
    }

    protected final void finalize() throws Throwable {
        throw new RuntimeException("Object#finalize() not implemented");
    }

    public final native Class<?> getClass();

    public native int hashCode();

    public final void notify() {
        throw new RuntimeException("Object#notify() not implemented");
    }

    public final void notifyAll() {
        throw new RuntimeException("Object#notifyAll() not implemented");
    }

    public String toString() {
        throw new RuntimeException("Object#toString() not implemented");
    }

    public final void wait(long timeout) {
        throw new RuntimeException("Object#wait(long) not implemented");
    }

    public final void wait(long timeout, int nanos) {
        throw new RuntimeException("Object#wait(long,int) not implemented");
    }

    public final void wait() {
        throw new RuntimeException("Object#wait() not implemented");
    }
}
