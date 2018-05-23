package java.util;

public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException() {
    }

    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException(Throwable cause) {
        super(cause);
    }

    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
