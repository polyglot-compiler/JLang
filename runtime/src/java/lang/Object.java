package java.lang;

public class Object {

    protected Object clone() throws CloneNotSupportedException {
        // if (this instanceof Cloneable)
        //     throw new CloneNotSupportedException(
        //         "Cannot clone " + getClass().getName());
        //
        // TODO: Object#clone() is unimplemented,
        //       but don't throw an exception for now.
        //       Note: would need to support array cloning here.
        return this;
    }

    public boolean equals(Object other) {
        return this == other;
    }

    protected final void finalize() throws Throwable {
        // Note: Marked as final to prevent overrides for now.
        throw new RuntimeException("Object#finalize() not implemented");
    }

    public final native Class getClass();

    public native int hashCode();

    public final void notify() {
        throw new RuntimeException("Object#notify() not implemented");
    }

    public final void notifyAll() {
        throw new RuntimeException("Object#notifyAll() not implemented");
    }

    public String toString() {
        // TODO: This is technically supposed to use Integer.toHexString(hashCode()).
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
