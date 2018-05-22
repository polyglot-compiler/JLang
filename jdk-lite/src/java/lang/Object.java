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
        return getClass().getName() + '@' + hashCode();
    }

    public final void wait(long timeout) throws InterruptedException {
        throw new RuntimeException("Object#wait(long) not implemented");
    }

    public final void wait(long timeout, int nanos) throws InterruptedException {
        throw new RuntimeException("Object#wait(long,int) not implemented");
    }

    public final void wait() throws InterruptedException {
        throw new RuntimeException("Object#wait() not implemented");
    }
}
